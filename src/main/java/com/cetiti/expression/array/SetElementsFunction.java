package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.*;

public class SetElementsFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "SetElements";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        // 参数个数检查 可变参数实现
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("CustomFunction requires 2 or 3 arguments.");
        }
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(args[0], env);

        Object value;
        if (args[1].getAviatorType() == AviatorType.Long) {
            value = ((AviatorLong) args[1]).longValue();
        } else if (args[1].getAviatorType() == AviatorType.Double) {
            value = ((AviatorDouble) args[1]).doubleValue();
        } else if (args[1].getAviatorType() == AviatorType.String) {
            value = args[1].stringValue(env);
        } else {
            value = FunctionUtils.getJavaObject(args[1], env);
        }
        if (args.length == 3) {
            String range = FunctionUtils.getStringValue(args[2], env);
            // 解析范围字符串 [a..b]
            int startIndex = Integer.parseInt(range.substring(1, range.indexOf("..")));
            int endIndex = Integer.parseInt(range.substring(range.indexOf("..") + 2, range.length() - 1));

            // 范围检查
            if (startIndex < 0 || endIndex >= array.size()) {
                throw new IllegalArgumentException("Invalid range specified.");
            }

            // 赋值
            for (int i = startIndex; i <= endIndex; i++) {
                array.set(i, value);
            }
        } else {
            Collections.fill(array, value);
        }
        return AviatorNumber.valueOf(0);
    }


    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new SetElementsFunction());

        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList("22", "33", "44", "77", "88"));

        String expression = "SetElements(array, 'xx','[0..3]')";
        System.out.println(AviatorEvaluator.execute(expression, env));

        System.out.println(env.get("array"));
    }


}
