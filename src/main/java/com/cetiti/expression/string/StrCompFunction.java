package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class StrCompFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String stringA = FunctionUtils.getStringValue(args[0], env);
        String stringB = FunctionUtils.getStringValue(args[1], env);

        int compareOption = FunctionUtils.getNumberValue(args[2], env).intValue();
        int maxChars = FunctionUtils.getNumberValue(args[3], env).intValue();

        if (compareOption == 1) {
            stringA = stringA.toLowerCase();
            stringB = stringB.toLowerCase();
        }

        if (maxChars == -1 || maxChars >= Math.max(stringA.length(), stringB.length())) {
            //直接比较
            return AviatorNumber.valueOf(stringA.compareTo(stringB));
        }
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

        int result = stringA.compareTo(stringB);

        return AviatorNumber.valueOf(result);
    }

    @Override
    public String getName() {
        return "StrComp";
    }


    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new StrCompFunction());

        String stringA = "Hello";
        String stringB = "Helloaa";
        int compareOption = 0;
        int maxChars = 7;

        Map<String, Object> env = new HashMap<>();
        env.put("stringA", stringA);
        env.put("stringB", stringB);
        env.put("compareOption", compareOption);
        env.put("maxChars", maxChars);

        String expression = "StrComp(stringA, stringB, compareOption, maxChars)";
        System.out.println(AviatorEvaluator.execute(expression, env));

    }
}
