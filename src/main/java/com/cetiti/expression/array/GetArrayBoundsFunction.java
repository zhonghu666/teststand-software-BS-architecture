package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorNumber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetArrayBoundsFunction extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        List<?> array = (List<?>) FunctionUtils.getJavaObject(arg1, env);
        String lowerBounds = getBounds(array, true);
        String upperBounds = getBounds(array, false);
        ;
        env.put("out" + ((AviatorJavaType) arg2).getName(), lowerBounds);
        env.put("out" + ((AviatorJavaType) arg3).getName(), upperBounds);

        return AviatorNumber.valueOf(0);
    }

    private String getBounds(List<?> array, boolean isLower) {
        if (array == null || array.isEmpty()) {
            return "";
        }

        int index = isLower ? 0 : array.size() - 1;

        Object element = array.get(index);
        if (element instanceof List) {
            return "[" + index + "]" + getBounds((List<?>) element, isLower);
        } else {
            return "[" + index + "]";
        }
    }

    @Override
    public String getName() {
        return "GetArrayBounds";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new GetArrayBoundsFunction());

        List<List<String>> arrayData = Arrays.asList(Arrays.asList("1", "2", "3"));
        Map<String, Object> env = new HashMap<>();
        env.put("arrayData", arrayData);
        env.put("lowerBounds","121");
        env.put("upperBounds","323");

        String expression = "GetArrayBounds(arrayData, lowerBounds, upperBounds)";
        Object result = AviatorEvaluator.execute(expression, env);

        System.out.println("Result: " + result);
        System.out.println("Lower Bounds: " + env.get("outlowerBounds"));
        System.out.println("Upper Bounds: " + env.get("outupperBounds"));

    }
}


