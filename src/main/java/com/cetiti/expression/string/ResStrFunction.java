package com.cetiti.expression.string;

import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class ResStrFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        //todo
        String categoryStr = FunctionUtils.getStringValue(args[0], env);
        String tagStr = FunctionUtils.getStringValue(args[1], env);
        if (args.length > 2) {
            String defaultStr = FunctionUtils.getStringValue(args[2], env);
        }
        boolean found = false;

        return null;
    }

    @Override
    public String getName() {
        return "ResStr";
    }
}
