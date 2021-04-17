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
import jp.co.syslinks.sscce.java.template.token.TokenShape;
import jp.co.syslinks.sscce.java.template.token.TokenValue;

public class TemplateParser {

    private enum Status {
        contents, tagBegin, tagIn, tagEnd;
    }

    private Status stauts = Status.contents;

    public void parse(Reader reader, Map<String, Object> map) throws Exception {
        List<TokenBase> tokenList = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        boolean stopMoveCursor = false;
        int read = -1;
        while (stopMoveCursor || (read = reader.read()) != -1) {
            char ch = (char) read;
            if (stauts == Status.contents) {
                if (ch == '#') {
                    stauts = Status.tagBegin;
                    if (buff.length() == 0) {
                        continue;
                    }
                    tokenList.add(new TokenContents(buff.toString()));
                    buff.setLength(0);
                    continue;
                }
                buff.append(ch);
            }
            if (stauts == Status.tagBegin) {
                if (ch == '[') {
                    stauts = Status.tagIn;
                    continue;
                }
            }
            if (stauts == Status.tagIn) {
                if (ch == ']') {
                    stauts = Status.tagEnd;
                    String ss = buff.toString();
                    if ("#".equals(ss) || ss.matches("^#+$")) {
                        tokenList.add(new TokenShape(ss));
                    } else if ("for".equals(ss)) {
                        tokenList.add(new TokenEndFor());
                    } else if ("if".equals(ss)) {
                        tokenList.add(new TokenEndIf());
                    } else if ("else".equals(ss)) {
                        tokenList.add(new TokenElse());
                    } else if (ss.startsWith("if ")) {
                        tokenList.add(new TokenIf(ss));
                    } else {
                        {
                            Matcher matcher = CONST.matcher(ss);
                            if (matcher.matches()) {
                                tokenList.add(new TokenConst(matcher.group(1)));
                            }
                        }
                        {
                            Matcher matcher = FOR1.matcher(ss);
                            if (matcher.matches()) {
                                tokenList.add(new TokenFor(matcher.group(1), matcher.group(2)));
                            }
                        }
                        {
                            Matcher matcher = VALUE.matcher(ss);
                            if (matcher.matches()) {
                                tokenList.add(new TokenValue(matcher.group(1), matcher.group(2)));
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
        }
        tokenList.add(new TokenContents(buff.toString()));
        buff.setLength(0);

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

        tokenList.forEach(v -> {
            System.out.println(v);
        });
        System.out.println("-----------------------------------------------------------------------------");
        render(tokenList, 0, -1, map, null);
    }

    final private void render(List<TokenBase> tokenList, int start, int end, Map<String, Object> map, Object forItem) throws Exception {
        for (int ii = start, len = end < 0 ? tokenList.size() : end; ii < len; ii++) {
            final TokenBase token = tokenList.get(ii);
            if (token instanceof TokenContents) {
                append(((TokenContents) token).contents);
            } else if (token instanceof TokenConst) {
                append(((TokenConst) token).value);
            } else if (token instanceof TokenValue) {
                append(getValue(((TokenValue) token), map, forItem));
            } else if (token instanceof TokenIf) {
                TokenIf tokenIf = (TokenIf) token;
                TokenElse tokenElse = (TokenElse) tokenList.get(tokenIf.endIndex);
                int startAt = -1;
                int endAt = -1;
                if (System.currentTimeMillis() % 2 == 0) { // dummy
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
            } else if (token instanceof TokenShape) {
                append(((TokenShape) token).value);
            }
        }
    }

    private void append(Object str) {
        System.out.print(str);
    }

    private Object getValue(TokenValue tokenValue, Map<String, Object> map, Object item) throws Exception {
        if (tokenValue.key2 == null) {
            if (item instanceof String) {
                return item;
            }
            return map.get(tokenValue.key);
        }
        return item.getClass().getField(tokenValue.key2.substring(1)).get(item);
    }

    private static final Pattern CONST = Pattern.compile("^\"(.*?)\"$"/*, Pattern.MULTILINE*/);
    private static final Pattern VALUE = Pattern.compile("^([a-zA-Z0-9]+)(\\.[a-zA-Z0-9]+)?$"/*, Pattern.MULTILINE*/);
    private static final Pattern FOR1 = Pattern.compile("for[ ]+([a-zA-Z0-9]+)[ ]+:[ ]+([a-zA-Z0-9]+)");

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
