package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexToOffsetFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "IndexToOffset";
    }


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);

        String index = FunctionUtils.getStringValue(arg2, env);
        Pattern pattern = Pattern.compile("\\[(\\d+)]");
        Matcher matcher = pattern.matcher(index);

        List<Integer> indices = new ArrayList<>();
        while (matcher.find()) {
            String number = matcher.group(1);
            indices.add(Integer.parseInt(number));
        }
        System.out.println(indices);
        // 计算偏移量
        int offset = calculateOffset(array, indices);

        return AviatorNumber.valueOf(offset);

    }


    public static int calculateOffset(List<Object> nestedList, List<Integer> indices) {
        int offset = 0;
        for (int index : indices) {
            offset += countElements(nestedList.subList(0, index));
            Object obj = nestedList.get(index);
            if (obj instanceof List) {
                nestedList = (List<Object>) obj;
            } else {
                break;
            }
        }
        return offset;
    }

    private static int countElements(List<Object> nestedList) {
        int count = 0;
        for (Object obj : nestedList) {
            if (obj instanceof List) {
                count += countElements((List<Object>) obj);
            } else {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new IndexToOffsetFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList(Arrays.asList("1", "2", "abc", "xx"), Arrays.asList("1", "2")));

        String expression = "IndexToOffset(array,'[1][1]')";
        System.out.println(AviatorEvaluator.execute(expression, env));

    }
}
