package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchPatternFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("MatchPattern function requires at least 2 parameters");
        }
        String string = FunctionUtils.getStringValue(args[0], env);
        String pattern = FunctionUtils.getStringValue(args[1], env);
        boolean ignoreCase = false;
        if (args.length > 2) {
            ignoreCase = FunctionUtils.getBooleanValue(args[2], env);
        }

        boolean result = string.matches(ignoreCase ? "(?i)" + pattern : pattern);

        if (args.length > 3) {
            String[] subPatterns = pattern.split("\\|");
            env.put(((AviatorJavaType) args[3]).getName(), Arrays.asList(subPatterns));
        }

        return AviatorBoolean.valueOf(result);
    }

    @Override
    public String getName() {
        return "MatchPattern";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new MatchPatternFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("str", "12d3");
        env.put("panted", "-?[1-9]\\d*");
        env.put("flag", true);
        env.put("subPatterns", "321");
        String expression = "MatchPattern(str, panted)";
        boolean result = (boolean) AviatorEvaluator.execute(expression, env);

        System.out.println("Result: " + result);

        //List<String> subPatterns = (List<String>) env.get("subPatterns");
        //System.out.println("Sub Patterns: " + subPatterns);
    }
}
