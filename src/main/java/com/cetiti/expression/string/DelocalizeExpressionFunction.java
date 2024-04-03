package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

public class DelocalizeExpressionFunction extends AbstractVariadicFunction {
    @Override
    public String getName() {
        return "DelocalizeExpression";
    }


    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        String localizedExpressionString = FunctionUtils.getStringValue(args[0], env);

        int decimalPointOption = 1;
        if (args.length > 1) {
            decimalPointOption = FunctionUtils.getNumberValue(args[1], env).intValue();
        }

        switch (decimalPointOption) {
            case 1:
                // 根据Station Options localization preferences决定使用操作系统设置还是默认设置
                return new AviatorString(localizedExpressionString);
            case 2:
                // 使用操作系统设置
                Locale defaultLocale = Locale.getDefault();
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(defaultLocale);
                String decimalSeparator = String.valueOf(symbols.getDecimalSeparator());
                return new AviatorString(localizedExpressionString.replace(decimalSeparator, "."));
            case 3:
                // 使用点号作为小数点
                return new AviatorString(localizedExpressionString);
            case 4:
                // 使用逗号作为小数点
                return new AviatorString(localizedExpressionString.replace(",", ".").replace(";", ","));
            default:
                throw new IllegalArgumentException("Invalid decimalPointOption: " + decimalPointOption);
        }
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new DelocalizeExpressionFunction());

        String deLocalizedExpression = (String) AviatorEvaluator.execute("DelocalizeExpression('3,14 + 2,5')");
        System.out.println("deLocalized Expression: " + deLocalizedExpression);
    }
}
