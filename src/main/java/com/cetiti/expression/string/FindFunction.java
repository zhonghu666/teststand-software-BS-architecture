package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class FindFunction extends AbstractVariadicFunction {


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2 || args.length > 5) {
            throw new IllegalArgumentException("RandomFunction requires 2 to 5 arguments.");
        }
        String str = FunctionUtils.getStringValue(args[0], env);
        // String to search for
        String searchStr = FunctionUtils.getStringValue(args[1], env);
        // Starting index for search
        int indexToSearchFrom = args.length > 2 ? FunctionUtils.getNumberValue(args[2], env).intValue() : 0;
        // Case sensitivity
        boolean ignoreCase = args.length > 3 && FunctionUtils.getBooleanValue(args[3], env);
        // Search direction
        boolean searchInReverse = args.length > 4 && FunctionUtils.getBooleanValue(args[4], env);

        if (ignoreCase) {
            str = str.toLowerCase();
            searchStr = searchStr.toLowerCase();
        }

        if (searchInReverse) {
            //todo 反转后搜索？？
            str = new StringBuilder(str).reverse().toString();
            searchStr = new StringBuilder(searchStr).reverse().toString();
            indexToSearchFrom = str.length() - indexToSearchFrom - 1;
        }
        int index = str.indexOf(searchStr, indexToSearchFrom);
        if (index == -1) {
            return AviatorLong.valueOf(-1);
        } else {
            return AviatorLong.valueOf(index);
        }
    }

    @Override
    public String getName() {
        return "Find";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new FindFunction());

        Map<String, Object> env = new HashMap<>();


        String expression = "Find('abcdefg','def',0,False,False)";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }


}
