package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class Float64Function extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object obj = arg1.getValue(env);
        if (obj instanceof Number) {
            return new AviatorDouble(((Number) obj).doubleValue());
        } else {
            try {
                return new AviatorDouble(Double.parseDouble(obj.toString()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert to double: " + obj);
            }
        }
    }

    @Override
    public String getName() {
        return "Float64";
    }
    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new Float64Function());
        // Now you can use the toDouble function in your expressions
        System.out.println(AviatorEvaluator.execute("Float64('123.456')"));
        System.out.println(AviatorEvaluator.execute("Float64(789)"));
    }
}
