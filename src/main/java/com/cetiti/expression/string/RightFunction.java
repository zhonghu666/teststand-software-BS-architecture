package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

public class RightFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Right";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String string = FunctionUtils.getStringValue(arg1, env);
        int numberOfCharacters = FunctionUtils.getNumberValue(arg2, env).intValue();


        if (numberOfCharacters >= string.length()) {
            return new AviatorString(string);
        } else {
            return new AviatorString(string.substring(string.length() - numberOfCharacters));
        }
    }


    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new RightFunction());

        String expression = "Right('fdgfdgreg', 3)";
        Object result = AviatorEvaluator.execute(expression);
        System.out.println(result);
    }

}
