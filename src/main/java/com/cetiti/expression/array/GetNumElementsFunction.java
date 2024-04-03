package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.*;

public class GetNumElementsFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "GetNumElements";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);
        List<Object> resultList = new ArrayList<>();
        getResultList(resultList, array);
        return AviatorNumber.valueOf(resultList.size());
    }

    //处理多维数组
    private void getResultList(List<Object> resultList, List<Object> array) {
        for (Object o : array) {
            if (o instanceof List) {
                getResultList(resultList, (List<Object>) o);
            } else {
                resultList.add(o);
            }
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new GetNumElementsFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList(Arrays.asList("1", "2", "abc", "xx"), Arrays.asList("1", "2")));

        String expression = "GetNumElements(array)";
        System.out.println(AviatorEvaluator.execute(expression, env));

    }
}
