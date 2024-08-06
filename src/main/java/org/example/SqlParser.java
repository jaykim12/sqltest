package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlParser {

    private static final String[] KEYWORDS = {"SELECT", "FROM", "WHERE", "AND", "LIKE"};
    private static final String[] FUNCTIONS = {"MAX", "SUM"};
    //a~z,A~Z _문자 포함
    private static final Pattern ElSE_STRING_PATTERN = Pattern.compile("^\\w*$");
    //숫자 찾는 패턴 (+는 1번이상 반복 -> 연속된 숫자가 나와야합니다.)
    private static final Pattern NUM_PATTERN = Pattern.compile("^\\d+$");
    //달러,문자,%,작은따옴표 포함된 패턴 찾음
    private static final Pattern STRING_PATTERN = Pattern.compile("^['\"].*['\"]$|^[\\$\\w]+[%]*$");
    //콜론으로 시작되는 패턴 찾기
    private static final Pattern BINDING_VARIABLE_PATTERN = Pattern.compile("^:.*$");

    public static void main(String[] args) {

        String sql = "SELECT MAX(EMPNO), SUM(SAL)\n" +
                "FROM EMP\n" +
                "WHERE DEPTNO = :in_deptno\n" +
                "AND ENAME LIKE '$xxx%'\n" +
                "-- AND ENAME LIKE '$' || :in_name\n" +
                "AND SAL > 8000 -- 이것도 주석";

        List<String> parsedResult = sqlParser(sql);
        for (String token : parsedResult) {
            System.out.println(token);
        }
    }

    private static List<String> sqlParser(String sql) {
        List<String> parsedResult = new ArrayList<>();

        // 줄 간격으로 나눕니다.
        String[] lines = sql.split("\\n");
        for (String line : lines) {
            // 각 문자열의 시작이 --이면 --> 주석을 더하고 출력
            if (line.trim().startsWith("--")) {
                parsedResult.add(line.trim() + " -> 주석");
            } else {
                // --의 시작 인덱스를 commentIndex에 넣습니다.
                int commentIndex = line.indexOf("--");
                // commentIndex가 -1이 아니라면 -> commentIndex에 --가 있으면 실행합니다.
                if (commentIndex != -1) {
                    // 처음부터 -- 시작되기 전까지의 코드 자릅니다.
                    String subPart = line.substring(0, commentIndex).trim();
                    // -- 이후 끝까지 자릅니다.
                    String mainPart = line.substring(commentIndex).trim();
                    // 만약 --가 포함되어 있지 않다면
                    if (!subPart.isEmpty()) {
                        // tokens 문자열에 정규식에 포함된 패턴들로 나눈다고 저장합니다.
                        String[] tokens = subPart.split("((?<=\\s)|(?=\\s)|(?<=[,()=<>!*/+\\-])|(?=[,()=<>!*/+\\-]))");
                        // tokens를 for-each문으로 돕니다. -> token String 변수에 저장한다.
                        for (String token : tokens) {
                            // 토큰 변수가 비어있다면 중지하고 다음 단계로 넘어갑니다.
                            if (token.trim().isEmpty()) {
                                continue;
                            }
                            //TokenType함수로 토큰의 유형을 결정합니다.
                            String tokenType = TokenType(token);
                            //결정 후 결과 목록에 토큰그대로의 문자와 결과를 같이 저장합니다.
                            parsedResult.add(tokenType);
                        }
                    }
                    parsedResult.add(mainPart + " -> 주석");
                    //문자앞에 --도 없고, 문자안에 --가 포함되어있지  않은 경우
                } else {
                    //위 과정과 동일하게 반복
                    String[] tokens = line.split("((?<=\\s)|(?=\\s)|(?<=[,()=<>!*/+\\-])|(?=[,()=<>!*/+\\-]))");
                    for (String token : tokens) {
                        if (token.trim().isEmpty()) {
                            continue;
                        }
                        String tokenType = TokenType(token);
                        parsedResult.add(tokenType);
                    }
                }
            }
        }

        return parsedResult;
    }

    // 토큰 타입 결정하는 함수
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
            return token + " -> 주석";  // 숫자를 주석으로 처리
        } else if (STRING_PATTERN.matcher(token).matches()) {
            return token + " -> 문자열";
        } else if (token.equals("(") || token.equals(")") || token.equals(",") || token.equals("=") || token.equals(">")) {
            return token + " -> ETC";
        } else {
            return token + " -> x";
        }
    }

    // 패턴이 아니기 때문에 문자열을 찾는 for문으로 따로 메소드를 만들었다
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
