package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class ChrFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Chr";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        int i = FunctionUtils.getNumberValue(arg1, env).intValue();
        char c = (char) i;
        return new AviatorString(String.valueOf(c));
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ChrFunction());

        Map<String, Object> env = new HashMap<>();
        String expression = "Chr(65)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
