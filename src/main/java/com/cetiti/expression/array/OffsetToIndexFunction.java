package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class OffsetToIndexFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "OffsetToIndex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);

        long offset = FunctionUtils.getNumberValue(arg2, env).longValue();

        List<Integer> indices = new ArrayList<>();
        boolean result = findIndicesHelper(array, offset, indices);
        if (result) {
            return new AviatorString(getIndexStr(indices));
        }
        return new AviatorString("");
    }

    private String getIndexStr(List<Integer> indices) {

        StringBuilder result = new StringBuilder();
        if (!CollectionUtils.isEmpty(indices)) {
            result = new StringBuilder("[");
            for (Integer index : indices) {
                result.append(index).append("][");
            }
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }
        return result.toString();
    }


    private boolean findIndicesHelper(List<Object> nestedList, long offset, List<Integer> indices) {
        int count = 0;
        for (int i = 0; i < nestedList.size(); i++) {
            Object obj = nestedList.get(i);
            if (obj instanceof List) {
                List<Object> sublist = (List<Object>) obj;
                int sublistSize = countElements(sublist);
                if (count + sublistSize > offset) {
                    indices.add(i);
                    boolean found = findIndicesHelper(sublist, offset - count, indices);
                    if (found) {
                        return true;
                    }
                }
                count += sublistSize;
            } else {
                count++;
                if (count > offset) {
                    indices.add(i);
                    return true;
                }
            }
        }
        return false;
    }


    private int countElements(List<Object> nestedList) {
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
        AviatorEvaluator.addFunction(new OffsetToIndexFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList(Arrays.asList("1", "2", "abc", "xx"), Arrays.asList("1", "2")));

        String expression = "OffsetToIndex(array, 4)";
        System.out.println(AviatorEvaluator.execute(expression, env));

    }
}
