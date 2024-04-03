package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class FindIndexFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "FindIndex";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Contains function expects 2 or 3 arguments");
        }

        AviatorObject arg1 = args[0];
        AviatorObject arg2 = args[1];

        List<?> array = (List<?>) FunctionUtils.getJavaObject(arg1, env);

        Object value;
        if (arg2.getAviatorType() == AviatorType.Long) {
            value = ((AviatorLong) arg2).longValue();
        } else if (arg2.getAviatorType() == AviatorType.Double) {
            value = ((AviatorDouble) arg2).doubleValue();
        } else if (arg2.getAviatorType() == AviatorType.String) {
            value = arg2.stringValue(env);
        } else {
            value = FunctionUtils.getJavaObject(arg2, env);
        }

        boolean caseSensitive = true;
        if (args.length == 3) {
            caseSensitive = FunctionUtils.getBooleanValue(args[2], env);
        }
        List<Integer> list = findIndex(array, value, caseSensitive);

        StringBuilder result = new StringBuilder();
        if (!CollectionUtils.isEmpty(list)) {
            result = new StringBuilder("[");
            for (Integer index : list) {
                result.append(index).append("][");
            }
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }
        return new AviatorString(result.toString());
    }

    private static List<Integer> findIndex(List<?> array, Object value, Boolean caseSensitive) {

        int index = -1;
        if (value instanceof String && !caseSensitive && array.get(0) instanceof String) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof String && ((String) value).equalsIgnoreCase((String) array.get(i))) {
                    index = i;
                }
            }
        } else {
            index = array.indexOf(value);
        }

        if (index != -1) {
            return List.of(index);
        }

        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {

            if (array.get(i) instanceof List) {
                // 当前元素依然是列表,递归调用
                List<Integer> subIndex = findIndex((List<?>) array.get(i), value, caseSensitive);
                if (!subIndex.isEmpty()) {
                    // 找到结果,添加当前索引
                    result.add(0, i);
                    result.addAll(subIndex);
                    return result;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new FindIndexFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList(Arrays.asList("2x2", "3x3", "4x4", "x77", "x88"),Arrays.asList("22x","7x7")));
        String expression = "FindIndex(array, '7x7')";
        Object execute = AviatorEvaluator.execute(expression, env);
        System.out.println(execute);
    }
}
