package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

public class TrimStartFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String untrimmedString = FunctionUtils.getStringValue(args[0], env);
        String charsToTrim = "";
        if (args.length > 1) {
            charsToTrim = FunctionUtils.getStringValue(args[1], env);
        }

        String trimmedString;
        if (charsToTrim.isEmpty()) {
            trimmedString = untrimmedString.stripLeading();
        } else {
            trimmedString = untrimmedString.replaceAll("^" + charsToTrim + "+", "");
        }

        return new AviatorString(trimmedString);
    }

    @Override
    public String getName() {
        return "TrimStart";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new TrimStartFunction());

        String expression = "TrimStart('%%%%   abc  dfg   %%%%','%')";
        String trimmedString = AviatorEvaluator.execute(expression).toString();

        System.out.println("Trimmed String: " + trimmedString);
    }
}
