package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class MidFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "Mid";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("RandomFunction requires 2 or 3 arguments.");
        }

        String string = FunctionUtils.getStringValue(args[0], env);
        int startIndex = FunctionUtils.getNumberValue(args[1], env).intValue();
        int numberOfCharacters = args.length > 2 ? FunctionUtils.getNumberValue(args[2], env).intValue() : -1;

        String result = mid(string, startIndex, numberOfCharacters);

        return new AviatorString(result);
    }

    private String mid(String string, int startIndex, int numberOfCharacters) {
        if (startIndex < 0 || startIndex >= string.length()) {
            return "";
        }
        if (numberOfCharacters < 0 || numberOfCharacters > string.length() - startIndex) {
            numberOfCharacters = string.length() - startIndex;
        }
        return string.substring(startIndex, startIndex + numberOfCharacters);
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new MidFunction());

        Map<String, Object> env = new HashMap<>();
        String expression = "Mid('abcdefg', 2, 3)";
        Object result = AviatorEvaluator.execute(expression, env);
        System.out.println(result);

    }
}
