package jp.co.syslinks.sscce.java.template;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.syslinks.sscce.java.template.token.TokenBase;
import jp.co.syslinks.sscce.java.template.token.TokenConst;
import jp.co.syslinks.sscce.java.template.token.TokenContents;
import jp.co.syslinks.sscce.java.template.token.TokenElse;
import jp.co.syslinks.sscce.java.template.token.TokenEndFor;
import jp.co.syslinks.sscce.java.template.token.TokenEndIf;
import jp.co.syslinks.sscce.java.template.token.TokenFor;
import jp.co.syslinks.sscce.java.template.token.TokenIf;
import jp.co.syslinks.sscce.java.template.token.TokenValue;

public class TemplateParser {

    private enum Status {
        contents, tagBegin, tagIn, tagEnd;
    }

    private Status stauts = Status.contents;

    public void parse(Reader reader, Map<String, Object> map) throws Exception {
        final List<TokenBase> tokenList = new ArrayList<>();
        {
            final StringBuilder buff = new StringBuilder();
            for (;;) {
                int read = reader.read();
                if (read == -1) {
                    break;
                }
                while (!this.readChar((char) read, tokenList, buff)) {
                }
            }
            if (buff.length() > 0) {
                tokenList.add(new TokenContents(buff.toString()));
                buff.setLength(0);
            }
        }
        {
            int forIndex = -1;
            int ifIndex = -1;
            int elseIndex = -1;
            for (int ii = 0, len = tokenList.size(); ii < len; ii++) {
                TokenBase token = tokenList.get(ii);
                if (token instanceof TokenFor) {
                    forIndex = ii;
                    continue;
                }
                if (token instanceof TokenEndFor) {
                    ((TokenFor) tokenList.get(forIndex)).endIndex = ii;
                    ((TokenEndFor) token).forIndex = forIndex;
                    continue;
                }
                if (token instanceof TokenIf) {
                    ((TokenIf) token).startIndex = ii;
                    ifIndex = ii;
                    elseIndex = ii;
                    continue;
                }
                if (token instanceof TokenElse) {
                    ((TokenIf) tokenList.get(elseIndex)).endIndex = ii;
                    ((TokenElse) token).startIndex = ii;
                    elseIndex = ii;
                    continue;
                }
                if (token instanceof TokenEndIf) {
                    ((TokenElse) tokenList.get(elseIndex)).endIndex = ii;
                    ((TokenEndIf) token).startIndex = ifIndex;
                    ((TokenEndIf) token).endIndex = ii;
                    continue;
                }
            }
        }
        {
            tokenList.forEach(v -> {
                System.out.println(v);
            });
            System.out.println("-----------------------------------------------------------------------------");
        }
        render(tokenList, 0, -1, map, null);
    }

    private boolean readChar(final char ch, final List<TokenBase> tokenList, final StringBuilder buff) throws Exception {
        boolean moveCursor = true;
        if (stauts == Status.contents) {
            if (ch == '#') {
                stauts = Status.tagBegin;
                if (buff.length() == 0) {
                    return moveCursor;
                }
                tokenList.add(new TokenContents(buff.toString()));
                buff.setLength(0);
                return moveCursor;
            }
            buff.append(ch);
        }
        if (stauts == Status.tagBegin) {
            if (ch == '[') {
                stauts = Status.tagIn;
                return moveCursor;
            } else {
                stauts = Status.contents;
                buff.append("#");
                moveCursor = false;
                return moveCursor;
            }
        }
        if (stauts == Status.tagIn) {
            if (ch == ']') {
                stauts = Status.tagEnd;
                String ss = buff.toString();
                if ("for".equals(ss)) { // end for
                    tokenList.add(new TokenEndFor());
                } else if ("if".equals(ss)) { // end if
                    tokenList.add(new TokenEndIf());
                } else if ("else".equals(ss)) {
                    tokenList.add(new TokenElse());
                } else {
                    boolean notMatched = true;
                    if (notMatched) {
                        Matcher matcher = CONST.matcher(ss);
                        if (matcher.matches()) {
                            tokenList.add(new TokenConst(matcher.group(1)));
                            notMatched = false;
                        }
                    }
                    if (notMatched) {
                        Matcher matcher = FOR1.matcher(ss);
                        if (matcher.matches()) {
                            tokenList.add(new TokenFor(matcher.group(1), matcher.group(2)));
                            notMatched = false;
                        }
                    }
                    if (notMatched) {
                        Matcher matcher = IF1.matcher(ss);
                        if (matcher.matches()) {
                            tokenList.add(new TokenIf(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5)));
                            notMatched = false;
                        }
                    }
                    if (notMatched) {
                        Matcher matcher = VALUE.matcher(ss);
                        if (matcher.matches()) {
                            tokenList.add(new TokenValue(matcher.group(1), matcher.group(2)));
                            notMatched = false;
                        }
                    }
                }
                buff.setLength(0);
            } else {
                buff.append(ch);
            }
        }
        if (stauts == Status.tagEnd) {
            stauts = Status.contents;
        }

        return moveCursor;
    }

    final private void render(List<TokenBase> tokenList, int start, int end, Map<String, Object> map, Object forItem) throws Exception {
        for (int ii = start, len = end < 0 ? tokenList.size() : end; ii < len; ii++) {
            final TokenBase token = tokenList.get(ii);
            if (token instanceof TokenContents) {
                append(((TokenContents) token).contents);
            } else if (token instanceof TokenConst) {
                append(((TokenConst) token).value);
            } else if (token instanceof TokenValue) {
                TokenValue tokenValue = (TokenValue) token;
                append(getValue(tokenValue.key, tokenValue.key2, map, forItem));
            } else if (token instanceof TokenIf) {
                TokenIf tokenIf = (TokenIf) token;
                TokenElse tokenElse = (TokenElse) tokenList.get(tokenIf.endIndex);
                int startAt = -1;
                int endAt = -1;
                // if (System.currentTimeMillis() % 2 == 0) { // dummy
                if (compare(getValue(tokenIf.left1, tokenIf.left2, map, forItem), tokenIf.cond, getValue(tokenIf.right1, tokenIf.right2, map, forItem))) {
                    startAt = tokenIf.startIndex + 1;
                    endAt = tokenIf.endIndex;
                } else {
                    startAt = tokenElse.startIndex + 1;
                    endAt = tokenElse.endIndex;
                }
                render(tokenList, startAt, endAt, map, forItem);

                TokenEndIf tokenEndIf = (TokenEndIf) tokenList.get(tokenElse.endIndex);
                ii = tokenEndIf.endIndex;
            } else if (token instanceof TokenFor) {
                TokenFor tokenFor = (TokenFor) token;
                Object obj = map.get(tokenFor.items);
                Iterator<?> iter = (Iterator<?>) obj.getClass().getMethod("iterator").invoke(obj);
                while (iter.hasNext()) {
                    render(tokenList, ii + 1, tokenFor.endIndex, map, iter.next());
                }
                ii = tokenFor.endIndex;
            }
        }
    }

    private boolean compare(Object obj1, String cond, Object obj2) {
        switch (cond) {
        case "==":
            if (obj1 == obj2) {
                return true;
            }
            if (obj1 == null && obj2 == null) {
                return true;
            }
            if (obj1 != null && obj2 != null) {
                if (obj1.equals(obj2)) {
                    return true;
                }
            }
            return false;
        default:
            throw new RuntimeException("not support cond : " + cond);
        }
    }

    private void append(Object str) {
        System.out.print(str);
    }

    private Object getValue(String key1, String key2, Map<String, Object> map, Object item) throws Exception {
        if (key2 == null) {
            if (key1.matches("^[0-9]+$")) {
                return Integer.parseInt(key1);
            }
            if (item instanceof String) {
                return item;
            }
            return map.get(key1);
        }
        return item.getClass().getField(key2).get(item);
    }

    private static final Pattern CONST = Pattern.compile("^\"(.*?)\"$"/*, Pattern.MULTILINE*/);
    private static final Pattern VALUE = Pattern.compile("^([a-zA-Z0-9]+)(?:\\.([a-zA-Z0-9]+))?$"/*, Pattern.MULTILINE*/); // username or user.name
    private static final Pattern FOR1 = Pattern.compile("for[ ]+([a-zA-Z0-9]+)[ ]+:[ ]+([a-zA-Z0-9]+)"); // for item : items

    /**
    if (user.name == 0)
    if (user.name > 0)
    if (user.name < 0)
    if (user.name != 0)
    if (user.name < user.name)
    if (user.name = 0)    NG
     */
    private static final Pattern IF1 = Pattern.compile("^if +\\(([a-zA-Z0-9]+)(?:\\.([a-zA-Z0-9]+))? +([!=<>]+) +([a-zA-Z0-9]+)(?:\\.([a-zA-Z0-9]+))?\\)$");

    public static void main(String[] args) throws Exception {
        try (Reader reader = new InputStreamReader(TemplateParser.class.getResourceAsStream("template.html"))) {
            Map<String, Object> map = new HashMap<>();
            map.put("user", "suzuki");
            map.put("items", Arrays.asList(new String[] { "1", "2", "3" }));
            map.put("userList",
                    Arrays.asList(new USER[] { //
                            new USER("wang", 20, 1) //
                            , new USER("zhang", 22, 0) //
                            , new USER("li", 30, 0) //
                            , new USER("sun", 42, 1) //
                    }));
            new TemplateParser().parse(reader, map);
        }
    }

    @SuppressWarnings("unused")
    private static final class USER {
        public final String name;
        public final int age;
        public final int sex;

        public USER(String name, int age, int sex) {
            this.name = name;
            this.age = age;
            this.sex = sex;
        }
    }
}
