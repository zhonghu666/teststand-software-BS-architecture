package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class CheckStrLimitFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String stringA = FunctionUtils.getStringValue(args[0], env);
        String stringB = FunctionUtils.getStringValue(args[1], env);
        String comparisonType = FunctionUtils.getStringValue(args[2], env);
        int maxChars = FunctionUtils.getNumberValue(args[3], env).intValue();

        boolean result = false;
        switch (comparisonType) {
            case "LOG":
                if (maxChars == -1 || maxChars >= Math.max(stringA.length(), stringB.length())) {
                    result = stringA.equals(stringB);
                } else {
                    String[] strArr = limitStr(stringA, stringB, maxChars);
                    result = strArr[0].equals(strArr[1]);
                }
                break;
            case "IgnoreCase":
                if (maxChars == -1 || maxChars >= Math.max(stringA.length(), stringB.length())) {
                    result = stringA.equalsIgnoreCase(stringB);
                } else {
                    String[] strArr = limitStr(stringA, stringB, maxChars);
                    result = strArr[0].equalsIgnoreCase(strArr[1]);
                }
                break;
            case "CaseSensitive":
                if (maxChars == -1 || maxChars >= Math.max(stringA.length(), stringB.length())) {
                    result = stringA.equals(stringB);
                } else {
                    String[] strArr = limitStr(stringA, stringB, maxChars);
                    result = strArr[0].equals(strArr[1]);
                }
                break;
            case "RegularExpressionIgnoreCase":
                if (maxChars == -1) {
                    result = stringA.matches("(?i)" + stringB);
                } else {
                    result = stringA.substring(0, Math.min(maxChars, stringA.length())).matches("(?i)" + stringB);
                }
                break;
            case "RegularExpressionCaseSensitive":
                if (maxChars == -1) {
                    result = stringA.matches(stringB);
                } else {
                    result = stringA.substring(0, Math.min(maxChars, stringA.length())).matches(stringB);
                }
                break;
        }

        // 返回结果
        if (result || comparisonType.equals("LOG")) {
            return new AviatorString("Passed");
        } else {
            return new AviatorString("Failed");
        }
    }

    private String[] limitStr(String stringA, String stringB, int maxChars) {
        String[] strArr = new String[2];
        int min = Math.min(stringA.length(), stringB.length());

        if (maxChars <= min) {
            stringA = stringA.substring(0, maxChars);
            stringB = stringB.substring(0, maxChars);
        } else {
            if (stringA.length() < stringB.length()) {
                stringB = stringB.substring(0, maxChars);
            } else {
                stringA = stringA.substring(0, maxChars);
            }
        }
        strArr[0] = stringA;
        strArr[1] = stringB;
        return strArr;
    }

    @Override
    public String getName() {
        return "CheckStrLimit";
    }

    public static void main(String[] args) {
        // 注册自定义函数
        AviatorEvaluator.addFunction(new CheckStrLimitFunction());

        // 准备测试数据
        String stringA = "Hello";
        String stringB = "World";
        String comparisonType = "IgnoreCase";
        int maxChars = 3;

        // 构建参数
        Map<String, Object> env = new HashMap<>();
        env.put("stringA", stringA);
        env.put("stringB", stringB);
        env.put("comparisonType", comparisonType);
        env.put("maxChars", maxChars);

        // 执行表达式
        String expression = "CheckStrLimit(stringA, stringB, comparisonType, maxChars)";
        System.out.println(AviatorEvaluator.execute(expression, env));

    }
}
