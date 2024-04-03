package com.cetiti.expression.numeric;

import com.alibaba.fastjson.JSON;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorJavaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Min function expects at least one argument");
        }

        if (args[0].getValue(env) instanceof List) {
            List<?> list = (List<?>) args[0].getValue(env);
            if (list.isEmpty()) return AviatorNil.NIL;
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                double value = ((Number) list.get(i)).doubleValue();
                if (value < min) {
                    min = value;
                    minIndex = i;
                }
            }
            if (args.length > 1) {
                // 确保第二个参数是用于存储索引的，而不是另一个数值
                if (args[1] instanceof AviatorJavaType) {
                    String indexStr = ((AviatorJavaType) args[1]).getName();
                    env.put("out" + indexStr, minIndex);
                }
            }
            return AviatorNumber.valueOf(min);
        } else {
            double min = Double.MAX_VALUE;
            for (AviatorObject arg : args) {
                double value = FunctionUtils.getNumberValue(arg, env).doubleValue();
                if (value < min) {
                    min = value;
                }
            }
            return AviatorNumber.valueOf(min);
        }
    }

    @Override
    public String getName() {
        return "Min";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new MinFunction());
        Object execute = AviatorEvaluator.execute("Min(1, 2, 3)");
        System.out.println(execute);
        // 或者对于一个集合
        List<Number> numbers = Arrays.asList(1, 2, 3,43,4512);
        Map<String, Object> env = new HashMap<>();
        env.put("numbers", numbers);
        env.put("index", 11);
        Object execute1 = AviatorEvaluator.execute("Min(numbers,index)", env);
        System.out.println(execute1);
        System.out.println(JSON.toJSON(env));

    }
}

