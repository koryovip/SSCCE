#!/usr/bin/env python3
"""
git-branch-tree: render local branches as vertical lanes (one per branch,
sorted by most recent activity) with curved connectors showing where each
branch forked from another.

Usage:
    python3 git_branch_tree.py [--repo PATH] [--limit N] [--out FILE]

Run it inside (or point --repo at) a local git repository. It shells out to
git only -- read-only, no network, no git state is changed.
"""
import argparse
import html
import subprocess
import sys
from datetime import datetime, timezone

FIELD_SEP = "\x1f"  # unit separator, unlikely to appear in commit subjects


def run_git(repo, *args):
    result = subprocess.run(
        ["git", "-C", repo, *args],
        capture_output=True, text=True, check=True,
    )
    return result.stdout


def get_branches(repo):
    """Local branches, unordered (caller sorts by topo_index)."""
    out = run_git(
        repo, "for-each-ref", "refs/heads/",
        f"--format=%(refname:short){FIELD_SEP}%(objectname){FIELD_SEP}%(committerdate:unix)",
    )
    branches = []
    for line in out.splitlines():
        if not line.strip():
            continue
        name, sha, ts = line.split(FIELD_SEP)
        branches.append({"name": name, "tip": sha, "ts": int(ts)})
    return branches


def get_commits(repo):
    """
    All commits reachable from any local branch, in git's --date-order:
    reverse-chronological but never showing a commit before any of its
    children. That guarantees parent always sorts after child even when
    two commits share the same second (common with fast/scripted commits),
    so we use this order -- not raw timestamps -- for layout.

    Returns (commits: hash -> info, topo_index: hash -> position).
    """
    out = run_git(
        repo, "log", "--all", "--branches", "--date-order",
        f"--format=%H{FIELD_SEP}%P{FIELD_SEP}%ct{FIELD_SEP}%an{FIELD_SEP}%s",
    )
    commits = {}
    topo_index = {}
    for i, line in enumerate(out.splitlines()):
        if not line.strip():
            continue
        h, parents, ts, author, subject = line.split(FIELD_SEP, 4)
        commits[h] = {
            "parents": parents.split() if parents else [],
            "ts": int(ts),
            "author": author,
            "subject": subject,
        }
        topo_index[h] = i
    return commits, topo_index


BASE_BRANCH_NAMES = ["main", "master", "trunk", "develop", "development"]


def detect_base_branch(repo, branches):
    """
    Best-effort guess at the repo's base/trunk branch: the remote's default
    branch if one is configured, else the first common trunk name that
    exists locally. Returns a branch name or None.
    """
    try:
        out = run_git(repo, "symbolic-ref", "refs/remotes/origin/HEAD")
        ref = out.strip().rsplit("/", 1)[-1]
        if any(b["name"] == ref for b in branches):
            return ref
    except subprocess.CalledProcessError:
        pass
    names = {b["name"] for b in branches}
    for candidate in BASE_BRANCH_NAMES:
        if candidate in names:
            return candidate
    return None


def order_branches(branches, topo_index):
    """
    Newest-tip-first. Real commit timestamps are the primary signal (they
    reflect true recency across branches); topo_index -- which guarantees
    a child never sorts after its parent -- only breaks exact ties, which
    mainly happen with fast scripted/CI commits sharing a second.
    """
    return sorted(
        branches,
        key=lambda b: (-b["ts"], topo_index.get(b["tip"], float("inf"))),
    )


def claim_lanes(branches, commits):
    """
    Walk each branch tip back along first-parent history, claiming commits
    for that branch's lane until hitting a commit already claimed by an
    earlier (more-recently-active) branch. That claimed commit becomes the
    fork point. A branch whose tip is already claimed by another branch
    (e.g. it sits exactly at another branch's tip, with no commits of its
    own yet) ends up with an empty lane -- handled at render time.

    Returns:
        lane_commits: {branch_name: [commit_hash, ...]}  tip-first order
        fork_of:      {branch_name: (parent_branch_name, fork_commit_hash)}
    """
    lane_of = {}
    lane_commits = {}
    fork_of = {}

    for b in branches:  # must already be ordered by order_branches()
        name = b["name"]
        claimed = []
        current = b["tip"]
        while current is not None and current not in lane_of and current in commits:
            lane_of[current] = name
            claimed.append(current)
            parents = commits[current]["parents"]
            current = parents[0] if parents else None
        lane_commits[name] = claimed
        if current is not None and current in lane_of and lane_of[current] != name:
            fork_of[name] = (lane_of[current], current)

    return lane_commits, fork_of


def relative_time(ts):
    now = datetime.now(timezone.utc).timestamp()
    delta = int(now - ts)
    for secs, unit in ((86400 * 365, "y"), (86400 * 30, "mo"), (86400, "d"),
                       (3600, "h"), (60, "m")):
        if delta >= secs:
            return f"{delta // secs}{unit} ago"
    return "just now"


PALETTE = [
    "#4f8ff7", "#f76d6d", "#5ec9a4", "#f7b955", "#c58af9",
    "#4fd1c5", "#f78fb3", "#8aa9f7", "#e0c341", "#6ee7b7",
]

LANE_W = 140
ROW_H = 34
TOP_PAD = 70
LEFT_PAD = 70
LABEL_LANE_GAP = 18


def build_svg(repo, branches, commits, lane_commits, fork_of, topo_index, limit):
    # trim each lane to the most recent `limit` commits (tip-first order)
    displayed = {}
    truncated = set()
    for b in branches:
        name = b["name"]
        full = lane_commits[name]
        displayed[name] = full[:limit]
        if len(full) > limit:
            truncated.add(name)

    # empty-lane branches (tip already claimed by another branch) still
    # need their tip's row/column resolved so we can draw a ref marker
    extra_rows = {
        b["tip"] for b in branches if not displayed[b["name"]] and b["tip"] in commits
    }

    # global row order follows git's own --date-order (topo_index), which
    # is stable even when commits share a timestamp -- avoids broken lanes
    all_shown = {h for name in displayed for h in displayed[name]} | extra_rows
    all_shown = sorted(all_shown, key=lambda h: topo_index.get(h, 0))
    row_of = {h: i for i, h in enumerate(all_shown)}

    n_rows = len(all_shown)
    n_lanes = len(branches)
    lane_x = {b["name"]: LEFT_PAD + i * LANE_W for i, b in enumerate(branches)}
    width = LEFT_PAD + n_lanes * LANE_W + 40
    height = TOP_PAD + (n_rows + 1) * ROW_H + 40

    svg = []
    svg.append(
        f'<svg viewBox="0 0 {width} {height}" xmlns="http://www.w3.org/2000/svg" '
        f'font-family="ui-monospace, SFMono-Regular, Menlo, Consolas, monospace">'
    )
    svg.append(
        f'<rect x="0" y="0" width="{width}" height="{height}" fill="#0d1117"/>'
    )

    def y_of(h):
        return TOP_PAD + row_of[h] * ROW_H

    color_of = {b["name"]: PALETTE[i % len(PALETTE)] for i, b in enumerate(branches)}

    # branch lane lines (drawn first, under dots/connectors)
    for b in branches:
        name = b["name"]
        pts = displayed[name]
        if len(pts) < 2:
            continue
        x = lane_x[name]
        y_top = y_of(pts[0])
        y_bot = y_of(pts[-1])
        svg.append(
            f'<line x1="{x}" y1="{y_top}" x2="{x}" y2="{y_bot}" '
            f'stroke="{color_of[name]}" stroke-width="3" stroke-linecap="round"/>'
        )

    # fork connectors: curve from parent lane (at fork commit row) up to
    # the child lane's oldest *displayed* commit
    for b in branches:
        name = b["name"]
        if name not in fork_of or not displayed[name]:
            continue
        parent_name, fork_hash = fork_of[name]
        if fork_hash not in row_of:
            continue  # fork point scrolled out of the displayed window
        x1, y1 = lane_x[parent_name], y_of(fork_hash)
        x2, y2 = lane_x[name], y_of(displayed[name][-1])
        mid_y = (y1 + y2) / 2
        svg.append(
            f'<path d="M {x1} {y1} C {x1} {mid_y}, {x2} {mid_y}, {x2} {y2}" '
            f'fill="none" stroke="{color_of[name]}" stroke-width="2" '
            f'stroke-dasharray="5,4" opacity="0.75"/>'
        )

    # commit dots + tooltips
    for b in branches:
        name = b["name"]
        for h in displayed[name]:
            x, y = lane_x[name], y_of(h)
            c = commits[h]
            title = html.escape(f"{h[:7]} {c['subject']}  ({c['author']}, {relative_time(c['ts'])})")
            svg.append(
                f'<circle cx="{x}" cy="{y}" r="5" fill="{color_of[name]}" '
                f'stroke="#0d1117" stroke-width="1.5"><title>{title}</title></circle>'
            )
        if name in truncated:
            x = lane_x[name]
            y = y_of(displayed[name][-1]) + ROW_H * 0.6
            svg.append(
                f'<text x="{x}" y="{y}" fill="#8b949e" font-size="11" text-anchor="middle">…</text>'
            )

    # branches with no commits of their own: they sit exactly on another
    # lane's dot (fork_of points at that shared commit) -- draw a small
    # ring around that dot and a short dashed tick out to this lane's
    # column so the ref is still visible instead of silently vanishing
    for b in branches:
        name = b["name"]
        if displayed[name] or name not in fork_of:
            continue
        owner_name, shared_hash = fork_of[name]
        if shared_hash not in row_of:
            continue
        ox, oy = lane_x[owner_name], y_of(shared_hash)
        x = lane_x[name]
        svg.append(
            f'<circle cx="{ox}" cy="{oy}" r="9" fill="none" '
            f'stroke="{color_of[name]}" stroke-width="2"/>'
        )
        svg.append(
            f'<path d="M {x} {TOP_PAD - 12} L {x} {oy} L {ox} {oy}" '
            f'fill="none" stroke="{color_of[name]}" stroke-width="2" stroke-dasharray="3,3"/>'
        )

    # lane labels at top
    for b in branches:
        name = b["name"]
        x = lane_x[name]
        label_y = TOP_PAD - LABEL_LANE_GAP - 18
        rel = relative_time(b["ts"])
        svg.append(
            f'<text x="{x}" y="{label_y}" fill="{color_of[name]}" font-size="13" '
            f'font-weight="600" text-anchor="middle">{html.escape(name)}</text>'
        )
        svg.append(
            f'<text x="{x}" y="{label_y + 16}" fill="#8b949e" font-size="11" '
            f'text-anchor="middle">{html.escape(rel)}</text>'
        )

    svg.append('</svg>')
    return "".join(svg), width, height


def build_html(svg, repo_name):
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<title>git branch tree - {html.escape(repo_name)}</title>
<style>
  :root {{ color-scheme: dark; }}
  body {{
    margin: 0; padding: 24px; background: #0d1117; color: #c9d1d9;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }}
  h1 {{ font-size: 15px; font-weight: 600; color: #8b949e; margin: 0 0 16px; }}
  .wrap {{ overflow-x: auto; border: 1px solid #21262d; border-radius: 8px; }}
</style>
</head>
<body>
<h1>branch tree &mdash; {html.escape(repo_name)} &mdash; newest branch furthest left</h1>
<div class="wrap">
{svg}
</div>
</body>
</html>
"""


def main():
    ap = argparse.ArgumentParser(description="Render local git branches as a lane diagram.")
    ap.add_argument("--repo", default=".", help="path to the git repo (default: current dir)")
    ap.add_argument("--limit", type=int, default=20, help="max commits shown per branch lane (default: 20)")
    ap.add_argument("--out", default="git-branch-tree.html", help="output HTML file")
    args = ap.parse_args()

    try:
        branches = get_branches(args.repo)
        commits, topo_index = get_commits(args.repo)
    except subprocess.CalledProcessError as e:
        sys.exit(f"git command failed: {e.stderr.strip()}")

    if not branches:
        sys.exit("no local branches found")

    branches = order_branches(branches, topo_index)

    # the base branch always claims shared trunk history first, regardless
    # of how recently it was touched -- otherwise a busy feature branch
    # that outpaces a stale main can "steal" main's own earlier commits
    base_name = detect_base_branch(args.repo, branches)
    if base_name:
        claim_order = [b for b in branches if b["name"] == base_name] + \
                      [b for b in branches if b["name"] != base_name]
    else:
        claim_order = branches
    lane_commits, fork_of = claim_lanes(claim_order, commits)
    svg, w, h = build_svg(args.repo, branches, commits, lane_commits, fork_of, topo_index, args.limit)
    repo_name = run_git(args.repo, "rev-parse", "--show-toplevel").strip().split("/")[-1]
    doc = build_html(svg, repo_name)

    with open(args.out, "w") as f:
        f.write(doc)

    print(f"wrote {args.out} ({w}x{h}px, {len(branches)} branches)")


if __name__ == "__main__":
    main()
