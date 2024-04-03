package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.*;

public class FindOffsetFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "FindOffset";
    }

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Contains function expects 2 or 3 arguments");
        }

        AviatorObject arg1 = args[0];
        AviatorObject arg2 = args[1];
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);

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
        List<Object> resultList = new ArrayList<>();
        List<Object> offset = findOffset(resultList, array);


        int index = -1;
        if (value instanceof String && !caseSensitive && offset.get(0) instanceof String) {
            for (int i = 0; i < offset.size(); i++) {
                if (offset.get(i) instanceof String && ((String) value).equalsIgnoreCase((String) offset.get(i))) {
                    index = i;
                }
            }
        } else {
            index = offset.indexOf(value);
        }

        return AviatorNumber.valueOf(index);

    }

    private List<Object> findOffset(List<Object> resultList, List<Object> array) {
        for (Object o : array) {
            if (o instanceof List) {
                findOffset(resultList, (List<Object>) o);
            } else {
                resultList.add(o);
            }
        }
        return resultList;
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new FindOffsetFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList(Arrays.asList("2x2", "3x3", "4x4", "x77", "x88"), Arrays.asList("22x", "7x7")));

        String expression = "FindOffset(array, '3x3')";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
