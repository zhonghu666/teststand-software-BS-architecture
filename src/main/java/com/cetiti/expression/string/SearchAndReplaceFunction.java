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
    public String getName() {
        return "SearchAndReplace";
    }

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String string = FunctionUtils.getStringValue(args[0], env);
        String searchString = FunctionUtils.getStringValue(args[1], env);
        String replacementString = FunctionUtils.getStringValue(args[2], env);

        int startIndex = args.length > 3 ? FunctionUtils.getNumberValue(args[3], env).intValue() : 0;
        boolean ignoreCase = args.length > 4 && FunctionUtils.getBooleanValue(args[4], env);
        int maxReplacements = args.length > 5 ? FunctionUtils.getNumberValue(args[5], env).intValue() : -1;
        boolean searchInReverse = args.length > 6 && FunctionUtils.getBooleanValue(args[6], env);

        int numReplacements = 0;
        String result = string;

        if (searchInReverse) {
            result = new StringBuilder(result).reverse().toString();
            startIndex = result.length() - startIndex - 1;
        }

        int index = startIndex;
        int count = 0;

        while (index >= 0 && index < result.length() && (maxReplacements == -1 || count < maxReplacements)) {
            index = findIndex(result, searchString, index, ignoreCase);
            if (index == -1) {
                break;
            }

            result = result.substring(0, index) + replacementString + result.substring(index + searchString.length());
            index += replacementString.length(); // Move past the replacement
            count++;
        }

        numReplacements = count;

        if (searchInReverse) {
            result = new StringBuilder(result).reverse().toString();
        }

        if (args.length > 7) {
            env.put(args[7].stringValue(env), numReplacements); // Optionally store the number of replacements
        }

        return new AviatorString(result);
    }

    private int findIndex(String source, String target, int fromIndex, boolean ignoreCase) {
        return ignoreCase ? source.toLowerCase().indexOf(target.toLowerCase(), fromIndex) : source.indexOf(target, fromIndex);
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new SearchAndReplaceFunction());
        String string = "Hello,World!";
        String searchString = "W";
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
      //  env.put("ignoreCase", ignoreCase);
       // env.put("maxReplacements", maxReplacements);
      //  env.put("searchInReverse", searchInReverse);
//env.put("numReplacements", numReplacements);

        String result = (String) AviatorEvaluator.execute("SearchAndReplace(string, searchString, replacementString,startIndex)", env);
        //int replacements = (int) env.get("numReplacements");

        System.out.println("Result: " + result);
        //System.out.println("Number of replacements: " + replacements);
    }


}
