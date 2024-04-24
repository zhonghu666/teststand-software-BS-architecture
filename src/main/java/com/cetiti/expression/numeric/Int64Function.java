package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class Int64Function extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object obj = arg1.getValue(env);
        if (obj instanceof Number) {
            return AviatorLong.valueOf(((Number) obj).longValue());
        } else {
            try {
                return AviatorLong.valueOf(Long.parseLong(obj.toString()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert to long: " + obj);
            }
        }
    }

    @Override
    public String getName() {
        return "Int64";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new Int64Function());
        // Now you can use the toLong function in your expressions
        System.out.println(AviatorEvaluator.execute("Int64('9223372036854775807')"));
        System.out.println(AviatorEvaluator.execute("Int64(123)"));
        System.out.println(AviatorEvaluator.execute("Int64('0x7fffffffffffffffi64')"));
    }

}
