package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class UInt64Function extends AbstractFunction {
    @Override
    public String getName() {
        return "UInt64";

    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {

        long number = FunctionUtils.getNumberValue(arg1, env).longValue();
        String unsignedNumber = Long.toUnsignedString(number);
        return new AviatorString(unsignedNumber);
    }

    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new UInt64Function());

        Map<String, Object> env = new HashMap<>();

        String expression = "UInt64(-123)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
