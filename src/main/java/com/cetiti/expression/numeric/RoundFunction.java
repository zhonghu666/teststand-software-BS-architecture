package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class RoundFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("RoundFunction requires 1 or 2 arguments.");
        }
        double number = FunctionUtils.getNumberValue(args[0], env).doubleValue();

        int option = 0;
        if (args.length == 2) {
            option = FunctionUtils.getNumberValue(args[1], env).intValue();
        }
        switch (option) {
            case 0:
                return AviatorDouble.valueOf(Math.floor(Math.abs(number)) * Math.signum(number));
            case 1:
                return AviatorDouble.valueOf(Math.ceil(Math.abs(number)) * Math.signum(number));
            case 2:
                return AviatorDouble.valueOf(Math.ceil(number));
            case 3:
                return AviatorDouble.valueOf(Math.floor(number));
            case 4:
                return AviatorDouble.valueOf(Math.rint(number));
            default:
                throw new IllegalArgumentException("Invalid rounding option: " + option);
        }
    }

    @Override
    public String getName() {
        return "Round";
    }


    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new RoundFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Round(-123.5)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
