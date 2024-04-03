package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPatternFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String string = FunctionUtils.getStringValue(args[0], env);
        String pattern = FunctionUtils.getStringValue(args[1], env);
        int indexToSearchFrom = FunctionUtils.getNumberValue(args[2], env).intValue();
        boolean ignoreCase = FunctionUtils.getBooleanValue(args[3], env);

        int patternLength = 0;
        int index = -1;

        String substring = string.substring(indexToSearchFrom);
        Pattern regexPattern;
        if (ignoreCase) {
            regexPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            regexPattern = Pattern.compile(pattern);
        }
        Matcher matcher = regexPattern.matcher(substring);


        if (matcher.find()) {
            //字符串长度
            patternLength = matcher.end() - matcher.start();
            //索引
            index = matcher.start() + indexToSearchFrom;
        }
        env.put(((AviatorJavaType) args[4]).getName(), patternLength);
        return AviatorLong.valueOf(index);
    }

    @Override
    public String getName() {
        return "FindPattern";
    }

    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new FindPatternFunction());

        String string = "Hello,World!";
        String pattern = "w[a-z]+";
        int indexToSearchFrom = 6;
        boolean ignoreCase = true;

        Map<String, Object> env = new HashMap<>();
        env.put("string", string);
        env.put("stringPatternToSearchFor", pattern);
        env.put("indexToSearchFrom", indexToSearchFrom);
        env.put("ignoreCase", ignoreCase);
        env.put("DSD","121");

        System.out.println(AviatorEvaluator.execute("FindPattern(string, stringPatternToSearchFor, indexToSearchFrom, ignoreCase,DSD)", env));
        System.out.println(env.get("DSD"));
    }
}
