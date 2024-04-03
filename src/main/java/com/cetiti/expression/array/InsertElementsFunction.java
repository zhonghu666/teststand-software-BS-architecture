package com.cetiti.expression.array;

import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class InsertElementsFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "InsertElements";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);
        array = Lists.newArrayList(array);
        //索引
        Number indexStr = FunctionUtils.getNumberValue(arg2, env);
        int index =  indexStr.intValue();
        //插入元素个数
        int number = FunctionUtils.getNumberValue(arg3, env).intValue();

        // 在List中插入新元素
        for (int i = 0; i < number; i++) {
            array.add(index++, getDefaultValue(array));
        }
        env.put(((AviatorJavaType) arg1).getName(), array);

        return AviatorNumber.valueOf(0);

    }

    private Object getDefaultValue(List<?> array) {
        if (CollectionUtils.isEmpty(array)) {
            return null;
        }
        Object o = array.get(0);
        if (o instanceof Boolean) {
            return false;
        } else if (o instanceof String) {
            return "";
        } else if (o instanceof Number) {
            return 0;
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new InsertElementsFunction());

        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList(false, false, true));

        String expression = "InsertElements(array, 2, 5)";
        System.out.println(AviatorEvaluator.execute(expression, env));

        System.out.println(env.get("array"));

    }
}
