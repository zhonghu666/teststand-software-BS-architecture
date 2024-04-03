package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class ValFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Val";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String str = FunctionUtils.getStringValue(arg1, env);
        try {
            env.put("isValid", true);
            return AviatorDouble.valueOf(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            env.put("isValid", false);
            return AviatorDouble.valueOf(0.0);
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ValFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Val('-123.3')";
        System.out.println(AviatorEvaluator.execute(expression, env));
        System.out.println(env.get("isValid"));
    }
}
