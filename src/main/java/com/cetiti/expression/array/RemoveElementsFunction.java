package com.cetiti.expression.array;

import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.*;

public class RemoveElementsFunction extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        List<?> array = (List<?>) FunctionUtils.getJavaObject(arg1, env);

        array = Lists.newArrayList(array);
        String indexStr = FunctionUtils.getStringValue(arg2, env);
        int index = Integer.parseInt(indexStr.replace("[", "").replace("]", ""));

        int number = FunctionUtils.getNumberValue(arg3, env).intValue();

        // 删除number元素个数
        for (int i = 0; i < number; i++) {
            array.remove(index);
        }
        env.put(((AviatorJavaType) arg1).getName(), array);
        return AviatorNumber.valueOf(0);
    }

    @Override
    public String getName() {
        return "RemoveElements";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new RemoveElementsFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList("22", "33", "44", "77", "88"));

        String expression = "RemoveElements(array, '[1]', 3)";
        System.out.println(AviatorEvaluator.execute(expression, env));
        System.out.println(env.get("array"));
    }
}
