package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class ExpFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Exp";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        if (arg1 instanceof AviatorDouble || arg1 instanceof AviatorLong) {
            double number = FunctionUtils.getNumberValue(arg1, env).doubleValue();
            return AviatorDouble.valueOf(Math.exp(number));
        } else {
            // 处理参数类型不匹配的情况
            throw new IllegalArgumentException("The Exp function only accepts a number as its argument.");
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ExpFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Exp(2.1)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
