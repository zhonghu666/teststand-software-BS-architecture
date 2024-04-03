package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        // todo
        Object value;
        if (args[0].getAviatorType() == AviatorType.Long) {
            value = ((AviatorLong) args[0]).longValue();
        } else if (args[0].getAviatorType() == AviatorType.Double) {
            value = ((AviatorDouble) args[0]).doubleValue();
        } else if (args[0].getAviatorType() == AviatorType.String) {
            value = args[0].stringValue(env);
        } else {
            value = FunctionUtils.getJavaObject(args[0], env);
        }
        String formatString = args.length > 1 ? FunctionUtils.getStringValue(args[1], env) : "%$.13g";
        int decimalPointOption = args.length > 2 ? FunctionUtils.getNumberValue(args[2], env).intValue() : 1;
        boolean useValueFormatIfDefined = args.length > 3 && FunctionUtils.getBooleanValue(args[3], env);
        String separator = args.length > 4 ? FunctionUtils.getStringValue(args[4], env) : "";

        // 执行Str函数的逻辑
        String formattedValue = "";

        if (value instanceof Number) {
            // 数字类型的值处理逻辑
            Number numberValue = (Number) value;
            // 根据formatString格式化数字
            formattedValue = String.format(formatString, numberValue);
        } else if (value instanceof String) {
            // 字符串类型的值处理逻辑
            String stringValue = (String) value;
            // 根据formatString格式化字符串
            formattedValue = String.format(formatString, stringValue);
        } else if (value instanceof List) {
            // 数组类型的值处理逻辑
            List<Object> array = (List<Object>) value;
            // 将数组元素用separator连接成字符串
            for (Object o : array) {
                formattedValue += o + separator;
            }
            formattedValue = formattedValue.substring(0, formattedValue.length() - 1);
        } else {
            // 其他类型的值处理逻辑
            formattedValue = value.toString();
        }

        // 返回结果
        return new AviatorString(formattedValue);
    }

    @Override
    public String getName() {
        return "Str";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new StrFunction());

        Map<String, Object> env = new HashMap<>();
        env.put("array", Arrays.asList("ttt", "aaa", "xxx"));

        System.out.println(AviatorEvaluator.execute("Str(3.324,'%.13g',1,false,'-')"));
        System.out.println(AviatorEvaluator.execute("Str('3.324','%s',1,false,'-')"));
        System.out.println(AviatorEvaluator.execute("Str(array,'%s',1,false,'-')", env));
    }
}
