package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

public class ReplaceFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "Replace";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3, AviatorObject arg4) {

        String string = FunctionUtils.getStringValue(arg1, env);
        int startIndex = FunctionUtils.getNumberValue(arg2, env).intValue();
        int numCharsToReplace = FunctionUtils.getNumberValue(arg3, env).intValue();
        String replacementString = FunctionUtils.getStringValue(arg4, env);

        String result = replace(string, startIndex, numCharsToReplace, replacementString);

        return new AviatorString(result);
    }

    private String replace(String string, int startIndex, int numCharsToReplace, String replacementString) {
        if (startIndex < 0 || startIndex >= string.length()) {
            return string;
        }

        if (numCharsToReplace < 0 || numCharsToReplace > string.length() - startIndex) {
            numCharsToReplace = string.length() - startIndex;
        }

        StringBuilder sb = new StringBuilder(string);
        sb.replace(startIndex, startIndex + numCharsToReplace, replacementString);
        return sb.toString();
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new ReplaceFunction());

        String expression = "Replace('dfghfjdgklr', 2, 3, '你好')";
        Object result = AviatorEvaluator.execute(expression);
        System.out.println(result);
    }
}
