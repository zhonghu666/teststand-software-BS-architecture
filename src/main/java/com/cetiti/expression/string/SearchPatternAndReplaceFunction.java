package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchPatternAndReplaceFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String string = FunctionUtils.getStringValue(args[0], env);
        String searchPattern = FunctionUtils.getStringValue(args[1], env);
        String replacementString = FunctionUtils.getStringValue(args[2], env);
        int startIndex = FunctionUtils.getNumberValue(args[3], env).intValue();
        boolean ignoreCase = FunctionUtils.getBooleanValue(args[4], env);
        int maxReplacements = FunctionUtils.getNumberValue(args[5], env).intValue();
        int numReplacements = 0;

        String startStr = "";
        if (startIndex > 0) {
            startStr = string.substring(0, startIndex);
            string = string.substring(startIndex);
        }

        Pattern regexPattern;
        if (ignoreCase) {
            regexPattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);
        } else {
            regexPattern = Pattern.compile(searchPattern);
        }
        Matcher matcher = regexPattern.matcher(string);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacementString);
            numReplacements++;
            if (maxReplacements > 0 && numReplacements >= maxReplacements) {
                break;
            }
        }
        matcher.appendTail(sb);
        sb.insert(0, startStr);

        env.put("numReplacements", numReplacements);
        return new AviatorString(sb.toString());
    }

    @Override
    public String getName() {
        return "SearchPatternAndReplace";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new SearchPatternAndReplaceFunction());

        String string = "Hello,World!Wa123";
        String searchPattern = "W[a-z]+";
        String replacementString = "xxx";
        int startIndex = 3;
        boolean ignoreCase = false;
        int maxReplacements = 1;
        int numReplacements = 0;

        Map<String, Object> env = new HashMap<>();
        env.put("string", string);
        env.put("searchPattern", searchPattern);
        env.put("replacementString", replacementString);
        env.put("startIndex", startIndex);
        env.put("ignoreCase", ignoreCase);
        env.put("maxReplacements", maxReplacements);
        env.put("numReplacements", numReplacements);

        System.out.println(AviatorEvaluator.execute("SearchPatternAndReplace(string, searchPattern, replacementString, startIndex, ignoreCase, maxReplacements, numReplacements)", env));
        System.out.println(env.get("numReplacements"));

    }
}
