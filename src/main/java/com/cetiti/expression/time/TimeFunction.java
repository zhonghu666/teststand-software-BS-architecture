package com.cetiti.expression.time;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

public class TimeFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        boolean force24HourFormat = false;
        long timeStampInSeconds;
        boolean baseTimeIsInitTime;

        LocalTime localTime = LocalTime.now();
        if (args.length > 0) {
            force24HourFormat = FunctionUtils.getBooleanValue(args[0], env);
        }

        if (args.length > 6) {
            timeStampInSeconds = FunctionUtils.getNumberValue(args[5], env).longValue();
            baseTimeIsInitTime = FunctionUtils.getBooleanValue(args[6], env);
            if (!baseTimeIsInitTime) {
                Instant instant = Instant.ofEpochMilli(timeStampInSeconds * 1000);
                localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime();
            } else {
                //todo
            }
        }
        int hours = localTime.getHour();
        int minutes = localTime.getMinute();
        int seconds = localTime.getSecond();

        if (args.length > 1) {
            env.put("hours", hours);
        }
        if (args.length > 2) {
            env.put("minutes", minutes);
        }
        if (args.length > 3) {
            env.put("seconds", seconds);
        }
        if (args.length > 4) {
            env.put("milliseconds", 0);
        }

        DateTimeFormatter formatter;
        if (force24HourFormat) {
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
        } else {
            formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        }

        String formattedTime = localTime.format(formatter);
        return new AviatorString(formattedTime);
    }

    @Override
    public String getName() {
        return "Time";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new TimeFunction());
        AviatorEvaluator.addFunction(new SecondsFunction());

        String expression = "Time(true, hours, minutes, seconds, milliseconds, Seconds(false), false)";
        Map<String, Object> env = AviatorEvaluator.newEnv();
        System.out.println(AviatorEvaluator.execute(expression, env));

        int hours = (int) env.get("hours");
        int minutes = (int) env.get("minutes");
        int seconds = (int) env.get("seconds");
        int milliseconds = (int) env.get("milliseconds");

        System.out.println("Hours: " + hours);
        System.out.println("Minutes: " + minutes);
        System.out.println("Seconds: " + seconds);
        System.out.println("Milliseconds: " + milliseconds);
    }
}
