package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class AscFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Asc";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String str = FunctionUtils.getStringValue(arg1, env);
        if (str != null && !str.isEmpty()) {
            int codePoint = str.codePointAt(0);
            return AviatorLong.valueOf(codePoint);
        } else {
            throw new IllegalArgumentException("String is empty or null");
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new AscFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("name","asdg");
        String expression = "Asc(name)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
