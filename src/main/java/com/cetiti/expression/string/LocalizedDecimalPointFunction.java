package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizedDecimalPointFunction extends AbstractVariadicFunction {
    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        int decimalPointOption = 1;
        System.out.println(args.length);
        if (args.length != 0) {
            decimalPointOption = FunctionUtils.getNumberValue(args[0], env).intValue();
        }
        switch (decimalPointOption) {
            case 1:
                // 根据Station Options localization preferences决定使用操作系统设置还是默认设置
                return new AviatorString(".");
            case 2:
                // 使用操作系统设置
                Locale defaultLocale = Locale.getDefault();
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(defaultLocale);
                String decimalSeparator = String.valueOf(symbols.getDecimalSeparator());
                return new AviatorString(decimalSeparator);
            case 3:
                // 使用点号作为小数点
                return new AviatorString(".");
            case 4:
                // 使用逗号作为小数点
                return new AviatorString(",");
            default:
                throw new IllegalArgumentException("Invalid decimalPointOption: " + decimalPointOption);
        }
    }

    @Override
    public String getName() {
        return "LocalizedDecimalPoint";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new LocalizedDecimalPointFunction());

        Map<String, Object> env = new HashMap<>();

        String expression = "LocalizedDecimalPoint()";
        System.out.println(AviatorEvaluator.execute(expression, env));
    }
}
