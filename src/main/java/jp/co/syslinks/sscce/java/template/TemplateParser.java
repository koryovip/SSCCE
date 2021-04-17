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
import jp.co.syslinks.sscce.java.template.token.TokenContents;
import jp.co.syslinks.sscce.java.template.token.TokenEnd;
import jp.co.syslinks.sscce.java.template.token.TokenFor;
import jp.co.syslinks.sscce.java.template.token.TokenValue;

public class TemplateParser {

    private enum Status {
        contents, tagBegin, tagIn, tagEnd;
    }

    private Status stauts = Status.contents;

    public void parse(Reader reader, Map<String, Object> map) throws Exception {
        List<TokenBase> tokenList = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        int read = -1;
        while ((read = reader.read()) != -1) {
            char ch = (char) read;
            if (stauts == Status.contents) {
                if (ch == '#') {
                    stauts = Status.tagBegin;
                    tokenList.add(new TokenContents(buff.toString()));
                    buff.setLength(0);
                    continue;
                }
                buff.append(ch);
            }
            if (stauts == Status.tagBegin) {
                if (ch == '(') {
                    stauts = Status.tagIn;
                    continue;
                }
            }
            if (stauts == Status.tagIn) {
                if (ch == ')') {
                    stauts = Status.tagEnd;
                    String ss = buff.toString();
                    if ("end".equals(ss)) {
                        tokenList.add(new TokenEnd());
                    } else {
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
        for (int ii = 0, len = tokenList.size(); ii < len; ii++) {
            TokenBase token = tokenList.get(ii);
            if (token instanceof TokenFor) {
                forIndex = ii;
                continue;
            }
            if (token instanceof TokenEnd) {
                ((TokenFor) tokenList.get(forIndex)).endIndex = ii;
                ((TokenEnd) token).forIndex = forIndex;
            }
        }

        tokenList.forEach(v -> {
            System.out.println(v);
        });
        System.out.println("-----------------------------------------------------------------------------");
        render(tokenList, 0, -1, map, null);
    }

    private void render(List<TokenBase> tokenList, int start, int end, Map<String, Object> map, Object forItem) throws Exception {
        for (int ii = start, len = end < 0 ? tokenList.size() : end; ii < len; ii++) {
            TokenBase token = tokenList.get(ii);
            if (token instanceof TokenContents) {
                append(((TokenContents) token).contents);
            } else if (token instanceof TokenValue) {
                append(getValue(((TokenValue) token), map, forItem));
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
