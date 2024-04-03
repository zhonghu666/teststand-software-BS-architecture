package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomFunction extends AbstractVariadicFunction {


    @Override
    public String getName() {
        return "Random";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("RandomFunction requires 2 or 3 arguments.");
        }
        double low = FunctionUtils.getNumberValue(args[0], env).doubleValue();
        double high = FunctionUtils.getNumberValue(args[1], env).doubleValue();
        double seed = System.currentTimeMillis();
        if (args.length == 3) {
            seed = FunctionUtils.getNumberValue(args[2], env).doubleValue();
        }
        Random random = new Random(Double.doubleToLongBits(seed));

        double result = low + (high - low) * random.nextDouble();
        return AviatorNumber.valueOf(result);
    }


    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new RandomFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Random(2,10,0.0)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
