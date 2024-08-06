package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlParserTwo {

    private static final String[] KEYWORDS = {"SELECT", "FROM", "WHERE", "AND", "LIKE"};
    private static final String[] FUNCTIONS = {"MAX", "SUM"};
    private static final Pattern ElSE_STRING_PATTERN = Pattern.compile("^\\w*$");
    private static final Pattern NUM_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^['\"].*['\"]$|^[\\$\\w]+[%]*$");
    private static final Pattern BINDING_VARIABLE_PATTERN = Pattern.compile("^:.*$");

    public static void main(String[] args) {
        String sql = "SELECT MAX(EMPNO), SUM(SAL)\n" +
                "FROM EMP\n" +
                "WHERE DEPTNO = :in_deptno\n" +
                "AND ENAME LIKE '$xxx%'\n" +
                "/** 이것도 multi line 주석 */\n" +
                "-- AND ENAME LIKE '$' || :in_name\n" +
                "AND SAL > 8000 -- 이것도 주석\n" +
                "/* 이것은 multi line 주석이다\n여러 line 의 주석을 처리할 수 있다.\n주석안에 있는 모든 token(keyword,Binding변수,문자열등)은 무시되고 주석으로 간주된다. */";

        List<String> parsedResult = sqlParser(sql);
        for (String token : parsedResult) {
            System.out.println(token);
        }
    }

    private static List<String> sqlParser(String sql) {
        List<String> parsedResult = new ArrayList<>();
        boolean MultiLine = false;
        boolean isString = false;
        // \\n 기준으로 분리
        String[] lines = sql.split("\n");
        for (String line : lines) {
            line = line.trim();
            int lineLength = line.length();
            for (int i = 0; i < lineLength; i++) {
                //현재 인덱스에 위치한 문자 -> c에 저장
                char c = line.charAt(i);

                // c가 큰따옴표 이면서 -> 이전문자가 백슬레시 아니거나 c가 첫번쨰 문자인경우
                if (c == '\"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                    //문자열을 true로 변경
                    isString = !isString;
                }
                //위 조건을 만족할때만 실행
                if (!isString) {
                    //multiline true이면
                    if (MultiLine) {
                        // */ 이후에 나오는 문자열이면 if문 실행
                        if (line.substring(i).startsWith("*/")) {
                            // */는 주석의 끝이므로 multiLine false;
                            MultiLine = false;
                            continue;
                        }
                        else {
                            // */가 발견되지 않는다면 현재 인덱스 i부터 끝까지 주석으로 간주 후 내부반복문 종료
                            parsedResult.add(line.substring(i) + " -> 주석");
                            break;
                        }
                        // /*문자열 감지하면 다중 주석의 시작
                    } else if (line.substring(i).startsWith("/*")) {
                        MultiLine = true; // 이부분 때문에 메소드를 만들었습니다.
                        continue;
                    }
                }
            }
            // multiLine이 트루이거나 line이 존재한다면 proccessline 메소드를 통해 처리
            if (!MultiLine && !line.isEmpty()) {
                parsedResult.addAll(processLine(line));
            }
        }

        return parsedResult;
    }
    //과제 1에서 만들었던 메소드를 두개로 나눴습니다.
    private static List<String> processLine(String line) {
        List<String> tokens = new ArrayList<>();
        int commentIndex = line.indexOf("--");
        if (commentIndex != -1) {
            String subPart = line.substring(0, commentIndex).trim();
            if (!subPart.isEmpty()) {
                tokens.addAll(processTokens(subPart));
            }
            tokens.add(line.substring(commentIndex).trim() + " -> 주석");
        } else {
            tokens.addAll(processTokens(line));
        }
        return tokens;
    }
    //과제 1에서 만들었던 메소드를 두개로 나눴습니다.
    private static List<String> processTokens(String line) {
        List<String> tokens = new ArrayList<>();
        String[] parts = line.split("((?<=\\s)|(?=\\s)|(?<=[,()=<>!*/+\\-])|(?=[,()=<>!*/+\\-]))");
        for (String token : parts) {
            if (!token.trim().isEmpty()) {
                tokens.add(TokenType(token));
            }
        }
        return tokens;
    }

    private static String TokenType(String token) {
        if (isKeyword(token)) {
            return token + " -> keyword";
        } else if (isFunction(token)) {
            return token + " -> function";
        } else if (BINDING_VARIABLE_PATTERN.matcher(token).matches()) {
            return token + " -> Binding 변수";
        } else if (ElSE_STRING_PATTERN.matcher(token).matches()) {
            return token + " -> ETC";
        } else if (NUM_PATTERN.matcher(token).matches()) {
            return token + " -> 숫자";
        } else if (STRING_PATTERN.matcher(token).matches()) {
            return token + " -> 문자열";
        } else if (token.equals("(") || token.equals(")") || token.equals(",") || token.equals("=") || token.equals(">")) {
            return token + " -> ETC";
        } else {
            return token + " -> x";
        }
    }

    private static boolean isKeyword(String token) {
        for (String keyword : KEYWORDS) {
            if (keyword.equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFunction(String token) {
        for (String function : FUNCTIONS) {
            if (function.equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }
}
