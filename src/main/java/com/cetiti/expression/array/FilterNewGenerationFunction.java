package com.cetiti.expression.array;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;


public class FilterNewGenerationFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "FilterNewGeneration";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        // 从AviatorObject参数中获取Java值
        List<?> array = (List<?>) arg1.getValue(env);
        String fieldName = (String) arg2.getValue(env);
        Object value = arg3.getValue(env);

        List<Object> result = new ArrayList<>();
        for (Object obj : array) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName); // 获取指定字段
                field.setAccessible(true); // 设置私有字段可访问
                if (value.equals(field.get(obj))) { // 比较字段值
                    result.add(obj);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new AviatorRuntimeJavaType(result);
    }
}
