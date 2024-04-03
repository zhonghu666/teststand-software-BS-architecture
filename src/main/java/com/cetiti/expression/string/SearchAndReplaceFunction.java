package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.Map;

public class SearchAndReplaceFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String string = FunctionUtils.getStringValue(args[0], env);
        String searchString = FunctionUtils.getStringValue(args[1], env);
        String replacementString = FunctionUtils.getStringValue(args[2], env);
        int startIndex = FunctionUtils.getNumberValue(args[3], env).intValue();
        boolean ignoreCase = FunctionUtils.getBooleanValue(args[4], env);
        int maxReplacements = FunctionUtils.getNumberValue(args[5], env).intValue();
        boolean searchInReverse = FunctionUtils.getBooleanValue(args[6], env);
//        int numReplacements = FunctionUtils.getNumberValue(args[7], env).intValue();

        int numReplacements = 0;
        String result = string;

        if (searchInReverse) {
            result = reverseString(result);
//            startIndex = result.length() - startIndex - 1;
        }

        int index = startIndex;
        int count = 0;

        while (index >= 0 && index < result.length()) {
            index = ignoreCase ? result.toLowerCase().indexOf(searchString.toLowerCase(), index) : result.indexOf(searchString, index);
            if (index == -1) {
                break;
            }

            result = result.substring(0, index) + replacementString + result.substring(index + searchString.length());
            index += replacementString.length();
            count++;
            numReplacements++;

            if (maxReplacements > 0 && count >= maxReplacements) {
                break;
            }
        }

        if (searchInReverse) {
            result = reverseString(result);
        }

        env.put("numReplacements", numReplacements);
        return new AviatorString(result);
    }

    private String reverseString(String string) {
        return new StringBuilder(string).reverse().toString();
    }

    @Override
    public String getName() {
        return "SearchAndReplace";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new SearchAndReplaceFunction());
        String string = "Hello,World!";
        String searchString = "O";
        String replacementString = "tt";
        int startIndex = 0;
        boolean ignoreCase = true;
        int maxReplacements = 1;
        boolean searchInReverse = true;
        int numReplacements = 0;

        Map<String, Object> env = new HashMap<>();
        env.put("string", string);
        env.put("searchString", searchString);
        env.put("replacementString", replacementString);
        env.put("startIndex", startIndex);
        env.put("ignoreCase", ignoreCase);
        env.put("maxReplacements", maxReplacements);
        env.put("searchInReverse", searchInReverse);
        env.put("numReplacements", numReplacements);

        String result = (String) AviatorEvaluator.execute("SearchAndReplace(string, searchString, replacementString, startIndex, ignoreCase, maxReplacements, searchInReverse, numReplacements)", env);
        int replacements = (int) env.get("numReplacements");

        System.out.println("Result: " + result);
        System.out.println("Number of replacements: " + replacements);
    }


}
