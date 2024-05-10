package com.cetiti.expression.array;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainsFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "Contains";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Contains function expects 2 or 3 arguments");
        }

        AviatorObject arg1 = args[0];
        AviatorObject arg2 = args[1];

        if (arg1.getAviatorType() != AviatorType.JavaType) {
            throw new IllegalArgumentException("Expected a JavaType for the array.");
        }

        List<?> array = (List<?>) FunctionUtils.getJavaObject(arg1, env);

        // Handle the value extraction
        Object value = null;
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
        for (Object obj : array) {
            if (caseSensitive) {
                if (obj.equals(value)) {
                    return AviatorBoolean.TRUE;
                }
            } else {
                if (obj instanceof String && value instanceof String && ((String) obj).equalsIgnoreCase((String) value)) {
                    return AviatorBoolean.TRUE;
                } else if (obj.equals(value)) {
                    return AviatorBoolean.TRUE;
                }
            }
        }

        return AviatorBoolean.FALSE;
    }


    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ContainsFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList("A", "b", "D"));

        String expression = "Contains(array, 'A')";
        Expression compiledExp = AviatorEvaluator.compile(expression);
        Boolean result = (Boolean) compiledExp.execute(env);
        System.out.println(result);
    }
}



