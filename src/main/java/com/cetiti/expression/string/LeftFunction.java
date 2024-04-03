package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class LeftFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Left";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String str = FunctionUtils.getStringValue(arg1, env);
        int number = FunctionUtils.getNumberValue(arg2, env).intValue();

        if (number >= str.length()) {
            return new AviatorString(str);
        } else {
            return new AviatorString(str.substring(0, number));
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new LeftFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Left('dxhjkg',5)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
