package com.cetiti.expression.numeric;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "Min";
    }

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("min 函数至少需要一个参数");
        }

        // 检查第一个参数是否为 List
        Object firstArg = args[0].getValue(env);
        if (firstArg instanceof List) {
            List<?> list = (List<?>) firstArg;
            if (list.isEmpty()) {
                throw new IllegalArgumentException("数组不能为空");
            }

            double min = Double.POSITIVE_INFINITY;
            int minIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                double value = ((Number) list.get(i)).doubleValue();
                if (value < min) {
                    min = value;
                    minIndex = i;
                }
            }

            if (args.length > 1 && minIndex != -1) {
                env.put("out" + ((AviatorJavaType) args[1]).getName(), minIndex);
            }
            return AviatorRuntimeJavaType.valueOf(min);
        } else {
            // 处理传递多个数字的情况
            double min = Double.POSITIVE_INFINITY;
            for (AviatorObject arg : args) {
                Number num = (Number) arg.getValue(env);
                if (num.doubleValue() < min) {
                    min = num.doubleValue();
                }
            }
            return AviatorRuntimeJavaType.valueOf(min);
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new MaxFunction());

        // 直接传递多个数字
        System.out.println(AviatorEvaluator.execute("Min(3, 1, 4, 1, 5, 9, 2, 6)")); // 输出 9.0

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        env.put("maxIndex", "");
        Object execute = AviatorEvaluator.execute("Min(array, maxIndex)",env);
        // 传递 List 和可选参数
        System.out.println(execute); // 输出 9.0
        System.out.println(env);

    }
}
