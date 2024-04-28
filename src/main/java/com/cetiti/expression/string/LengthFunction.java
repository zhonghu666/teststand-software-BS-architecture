package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class LengthFunction extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String str = arg1.stringValue(env);  // Convert the first argument to a string
        if (str == null) {
            return AviatorLong.valueOf(0);  // Return 0 if the string is null
        }
        return AviatorLong.valueOf(str.length());  // Return the length of the string
    }

    @Override
    public String getName() {
        return "Len";  // The name of the function to be used in expressions
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new LengthFunction());
        // Now you can use the length function in your expressions
        System.out.println(AviatorEvaluator.execute("Len('Hello, world!')"));  // Outputs: 13
        System.out.println(AviatorEvaluator.execute("Len('')"));  // Outputs: 0
    }
}
