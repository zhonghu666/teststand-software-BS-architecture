package com.cetiti.expression.array;

import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetNumElementsFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "SetNumElements";
    }


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);
        array = Lists.newArrayList(array);

        int number = FunctionUtils.getNumberValue(arg2, env).intValue();

        for (int i = array.size(); i < number; i++) {
            array.add(getDefaultValue(array));
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

        AviatorEvaluator.addFunction(new SetNumElementsFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList("22", "33", "44", "77"));

        String expression = "SetNumElements(array, 6)";
        System.out.println(AviatorEvaluator.execute(expression, env));
        System.out.println(env.get("array"));
    }
}
