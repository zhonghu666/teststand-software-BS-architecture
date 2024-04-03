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

        if (arg1 instanceof AviatorString) {
            String str = FunctionUtils.getStringValue(arg1, env);
            if (str.length() > 0) {
                return AviatorLong.valueOf(str.charAt(0));
            } else {
                // 处理空字符串的情况
                return AviatorLong.valueOf(0);
            }
        } else {
            // 处理参数类型不匹配的情况
            throw new IllegalArgumentException("The Asc function only accepts a string as its argument.");
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new AscFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "Asc('a.3')";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
