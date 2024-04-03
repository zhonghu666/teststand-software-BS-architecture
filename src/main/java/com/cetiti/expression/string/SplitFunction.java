package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import java.util.*;
import java.util.regex.Pattern;

public class SplitFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Split";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String string = FunctionUtils.getStringValue(arg1, env);
        Object delimiters = arg2.getValue(env);
        List<String> tokens;
        if (delimiters instanceof String) {
            tokens = splitString(string, (String) delimiters);
        } else if (delimiters instanceof List) {
            tokens = splitString(string, (List<String>) delimiters);
        } else {
            throw new IllegalArgumentException("Invalid delimiter type: " + delimiters.getClass().getSimpleName());
        }
        return AviatorRuntimeJavaType.valueOf(tokens);
    }

    private List<String> splitString(String str, String separators) {
        String regex = buildRegex(separators);
        String[] parts = str.split(regex);
        return List.of(parts);
    }

    private String buildRegex(String separators) {
        StringBuilder regexBuilder = new StringBuilder();
        for (char separator : separators.toCharArray()) {
            regexBuilder.append("\\").append(separator).append("|");
        }
        regexBuilder.deleteCharAt(regexBuilder.length() - 1); // 删除最后一个多余的 |
        return regexBuilder.toString();
    }


    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new SplitFunction());
        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList("abc", "def"));
        System.out.println(AviatorEvaluator.execute("Split('23abcfghgdef4def00abc',array)", env));
        System.out.println(AviatorEvaluator.execute("Split('123#ds*dfgh*fkdlj#d','*#')", env));

    }

    private List<String> splitString(String str, List<String> delimiters) {
        String regex = buildRegex(delimiters);
        String[] parts = str.split(regex);
        return List.of(parts);
    }

    private String buildRegex(List<String> delimiters) {
        StringBuilder regexBuilder = new StringBuilder();
        for (String delimiter : delimiters) {
            regexBuilder.append(Pattern.quote(delimiter)).append("|");
        }
        regexBuilder.deleteCharAt(regexBuilder.length() - 1); // 删除最后一个多余的 |
        return regexBuilder.toString();
    }

}
