package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class ToLowerFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {

        String string = FunctionUtils.getStringValue(args[0], env);
        int startIndex = FunctionUtils.getNumberValue(args[1], env).intValue();
        int numCharacters = FunctionUtils.getNumberValue(args[2], env).intValue();
        boolean reverse = FunctionUtils.getBooleanValue(args[3], env);

        if (startIndex < 0 || startIndex >= string.length()) {
            return new AviatorString(string);
        }

        if (numCharacters == -1 || numCharacters > string.length() - startIndex) {
            numCharacters = string.length() - startIndex;
        }

        StringBuilder result = new StringBuilder(string);

        if (reverse) {
            result.reverse();
        }


        for (int i = startIndex; i < startIndex + numCharacters; i++) {
            char c = result.charAt(i);
            if (Character.isLetter(c)) {
                result.setCharAt(i, Character.toLowerCase(c));
            }
        }
        if (reverse) {
            result.reverse();
        }

        return new AviatorString(result.toString());
    }

    @Override
    public String getName() {
        return "ToLower";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ToLowerFunction());

        Map<String, Object> env = new HashMap<>();

        String input = "1AAAAAAA2";
        env.put("input", input);

        String result = (String) AviatorEvaluator.execute("ToLower(input,0,3,false)", env);
        System.out.println("Result: " + result);

    }
}
