package com.cetiti.expression.array;

import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import java.util.*;

public class SortFunction extends AbstractVariadicFunction {

    @Override
    public String getName() {
        return "Sort";
    }

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 4) {
            throw new IllegalArgumentException("Contains function expects 2 or 4 arguments");
        }

        AviatorObject arg1 = args[0];
        List<?> array = (List<?>) FunctionUtils.getJavaObject(arg1, env);

        boolean inPlace = FunctionUtils.getBooleanValue(args[1], env);
        boolean stable = false;
        boolean caseSensitive = true;
        if (args.length == 3) {
            stable = FunctionUtils.getBooleanValue(args[2], env);
        } else if (args.length == 4) {
            stable = FunctionUtils.getBooleanValue(args[2], env);
            caseSensitive = FunctionUtils.getBooleanValue(args[3], env);
        }

        List<?> sortList;
        if (inPlace) {
            sortList = array;
        } else {
            sortList = Lists.newArrayList(array);
        }
        if (stable) {
            sortList.sort(createComparator(caseSensitive));
        } else {
            sortList.sort((Comparator.comparing(String::valueOf)));
        }

        return AviatorRuntimeJavaType.valueOf(sortList);
    }

    private static Comparator<Object> createComparator(boolean caseSensitive) {
        if (caseSensitive) {
            return Comparator.comparing(String::valueOf);
        } else {
            return (o1, o2) -> String.valueOf(o1).compareToIgnoreCase(String.valueOf(o2));
        }

    }

    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new SortFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList("a","F","B","F","t"));

        String expression = "Sort(array, false,true)";
        System.out.println(AviatorEvaluator.execute(expression, env));
        System.out.println(env.get("array"));


    }
}
