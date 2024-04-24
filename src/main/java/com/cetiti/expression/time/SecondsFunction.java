package com.cetiti.expression.time;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SecondsFunction extends AbstractVariadicFunction {

    private static final Instant appStartTime = Instant.now(); // Capture the start time when the class is loaded

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        boolean useEpoch = false; // Default to using application start time

        if (args != null && args.length > 0) {
            useEpoch = FunctionUtils.getBooleanValue(args[0], env);
        }

        long elapsedSeconds;
        if (useEpoch) {
            elapsedSeconds = ChronoUnit.SECONDS.between(Instant.EPOCH, Instant.now());
        } else {
            elapsedSeconds = ChronoUnit.SECONDS.between(appStartTime, Instant.now());
        }

        return AviatorRuntimeJavaType.valueOf(elapsedSeconds);
    }

    @Override
    public String getName() {
        return "Seconds";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new SecondsFunction());

        String expression = "Seconds()";
        System.out.println(AviatorEvaluator.execute(expression));

        expression = "Seconds(false)";
        System.out.println(AviatorEvaluator.execute(expression));

        expression = "Seconds(true)";
        System.out.println(AviatorEvaluator.execute(expression));
    }
}
