package com.cetiti.expression.time;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class DateFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {

        boolean longFormat = false;
        if (args.length > 0) {
            longFormat = FunctionUtils.getBooleanValue(args[0], env);
        }
        LocalDate localDate = LocalDate.now();
        if (args.length > 6) {
            long timeStampInSeconds = FunctionUtils.getNumberValue(args[5], env).longValue();
            boolean baseTimeIsInitTime = FunctionUtils.getBooleanValue(args[6], env);
            if (!baseTimeIsInitTime) {
                Instant instant = Instant.ofEpochMilli(timeStampInSeconds * 1000);
                localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                //todo
            }
        }

        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int monthDay = localDate.getDayOfMonth();
        int weekDay = localDate.getDayOfWeek().getValue() + 1;
        //星期天是1
        if (weekDay == 8) {
            weekDay = 1;
        }

        if (args.length > 1) {
            env.put("year", year);
        }
        if (args.length > 2) {
            env.put("month", month);
        }
        if (args.length > 3) {
            env.put("monthDay", monthDay);
        }
        if (args.length > 4) {
            env.put("weekDay", weekDay);
        }
        String currentDate = longFormat ?
                localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
                localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()));
        return new AviatorString(currentDate);
    }

    @Override
    public String getName() {
        return "Date";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new DateFunction());
        AviatorEvaluator.addFunction(new SecondsFunction());
        Map<String, Object> env = new HashMap<>();

        String expression = "Date()";
        System.out.println(AviatorEvaluator.execute(expression, env));


        expression = "Date(true)";
        System.out.println(AviatorEvaluator.execute(expression, env));

        expression = "Date(false, year, month, monthDay, weekDay, Seconds(false), true)";
        AviatorEvaluator.execute(expression, env);
        int year = (int) env.get("year");
        int month = (int) env.get("month");
        int monthDay = (int) env.get("monthDay");
        int weekDay = (int) env.get("weekDay");
        System.out.println(year + "---" + month + "---" + monthDay + " (Weekday: " + weekDay + ")");
    }
}
