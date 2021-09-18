package jp.co.syslinks.sscce.java.cmd;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

import jp.co.syslinks.sscce.java.cmd.CDTrack.CDTrack_chapters;
import jp.co.syslinks.sscce.java.cmd.InputStreamThread.OutType;

/**
 * https://www.ffmpeg.org/download.html#build-windows
 * https://www.ne.jp/asahi/hishidama/home/tech/java/process.html
 * https://yatta47.hateblo.jp/entry/2015/03/03/231204
 * https://superuser.com/questions/1603510/ffmpeg-how-to-extract-a-single-audio-track-from-a-cd
 *
 * まず、track 情報を json 形式で取得。ここCDドライブのIドライブ（Windowsの場合）
 * ffprobe -f libcdio -i i: -print_format json -show_chapters -loglevel error > track.json
 * track １つづつやる
 * ffmpeg -f libcdio -ss 0          -t 35.693333 -i I: track01.mp3
 * ffmpeg -f libcdio -ss 35.693333  -t 24.266667 -i I: track02.mp3
 */
public class CD2Mp3 {

    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();
        CDTrack track = gson.fromJson(new String(Files.readAllBytes(Paths.get(CD2Mp3.class.getResource("track.json").toURI()))), CDTrack.class);
        for (CDTrack_chapters row : track.chapters) {
            final String ss = row.start_time.toPlainString();
            final String t = row.end_time.subtract(row.start_time).toPlainString();
            System.out.println(String.format("ffmpeg -loglevel error -f libcdio -ss %11s -t %10s -i I: track%02d.mp3", ss, t, row.id + 1));
        }
        System.out.print("continue? > ");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
        for (CDTrack_chapters row : track.chapters) {
            final String ss = row.start_time.toPlainString(); // 開始時間
            final String t = row.end_time.subtract(row.start_time).toPlainString(); // 長さ
            // System.out.println(String.format("ffmpeg -f libcdio -ss %11s -t %10s -i I: track%02d.mp3", ss, t, row.id + 1));
            List<String> cmd = new ArrayList<>();
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add("ffmpeg.exe");
            // cmd.add("-h");
            cmd.add("-y"); // 上書き確認しない
            cmd.add("-loglevel"); // loglevel
            cmd.add("error");
            cmd.add("-f");
            cmd.add("libcdio");
            cmd.add("-ss");
            cmd.add(ss);
            cmd.add("-t");
            cmd.add(t);
            cmd.add("-i");
            cmd.add("I:"); // CDドライブ
            cmd.add(String.format("track%02d.mp3", row.id + 1));
            System.out.println(String.join(" ", cmd));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File("R:/ffmpeg-4.4/bin"));
            // pb.redirectErrorStream(true);
            // 2. プロセスを開始する
            Process process = pb.start();

            //InputStreamのスレッド開始
            InputStreamThread it = new InputStreamThread(process.getInputStream(), OutType.std, Charset.defaultCharset());
            InputStreamThread et = new InputStreamThread(process.getErrorStream(), OutType.err, Charset.defaultCharset());
            it.start();
            et.start();

            //プロセスの終了待ち
            process.waitFor();

            //InputStreamのスレッド終了待ち
            it.join();
            et.join();

            System.out.println("戻り値：" + process.exitValue());

            //標準出力の内容を出力
            for (String s : it.getStringList()) {
                System.out.println(s);
            }
            //標準エラーの内容を出力
            for (String s : et.getStringList()) {
                System.err.println(s);
            }
        }
    }
}
