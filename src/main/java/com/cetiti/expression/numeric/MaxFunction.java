package com.cetiti.expression.numeric;

import com.alibaba.fastjson.JSON;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.*;

public class MaxFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Max function expects at least one argument");
        }

        // 检查是否为列表参数
        if (args[0].getValue(env) instanceof List) {
            List<?> list = (List<?>) args[0].getValue(env);
            if (list.isEmpty()) return AviatorNil.NIL;
            double max = Double.MIN_VALUE;
            int maxIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                double value = ((Number) list.get(i)).doubleValue();
                if (value > max) {
                    max = value;
                    maxIndex = i;
                }
            }
            if (args.length > 1) {
                // 处理可选的索引参数
                env.put("out" + ((AviatorJavaType) args[1]).getName(), maxIndex);
            }
            return AviatorNumber.valueOf(max);
        } else {
            // 处理一组数字参数
            double max = Double.MIN_VALUE;
            for (AviatorObject arg : args) {
                double value = FunctionUtils.getNumberValue(arg, env).doubleValue();
                if (value > max) {
                    max = value;
                }
            }
            return AviatorNumber.valueOf(max);
        }
    }

    @Override
    public String getName() {
        return "Max";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new MaxFunction());
        Object execute = AviatorEvaluator.execute("Max(1, 2, 3)");
        System.out.println(execute);
        // 或者对于一个集合
        List<Number> numbers = Arrays.asList(1, 2, 3,43,4512);
        Map<String, Object> env = new HashMap<>();
        env.put("numbers", numbers);
        env.put("index", 11);
        Object execute1 = AviatorEvaluator.execute("Max(numbers,index)", env);
        System.out.println(execute1);
        System.out.println(JSON.toJSON(env));

    }
}

