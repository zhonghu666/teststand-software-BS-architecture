package com.cetiti.expression.time;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SecondsFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        boolean returnSecondsSinceStartup = true;
        if (args.length > 0) {
            returnSecondsSinceStartup = FunctionUtils.getBooleanValue(args[0], env);
        }

        long seconds;
        if (returnSecondsSinceStartup) {
            //todo 如果传递默认参数True，或者不传递参数，该函数将返回应用程序初始化TestStand Engine以来的秒数
            long startTime = System.currentTimeMillis(); // Replace with your actual base time
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        } else {
            long currentTime = System.currentTimeMillis();
            seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime);
        }
        return AviatorLong.valueOf(seconds);
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

    }
}
