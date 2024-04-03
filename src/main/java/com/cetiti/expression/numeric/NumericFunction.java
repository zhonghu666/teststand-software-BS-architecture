package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.system.DoubleFunction;
import com.googlecode.aviator.runtime.function.system.LongFunction;
import io.lettuce.core.ScriptOutputType;

public class NumericFunction {

    public static void main(String[] args) {
        // Abs()
        Double result1 = (Double) AviatorEvaluator.execute("math.abs(-1.5)");
        System.out.println(result1);

        //ACos()
        Double result2 = (Double) AviatorEvaluator.execute("math.acos(0.5)");
        System.out.println(result2);

        //Asc()

        //ASin()
        Double result3 = (Double) AviatorEvaluator.execute("math.asin(0.5)");
        System.out.println(result3);

        //ATan()
        Double result4 = (Double) AviatorEvaluator.execute("math.atan(0.5)");
        System.out.println(result4);

        //Cos()
        Double result5 = (Double) AviatorEvaluator.execute("math.cos(0.5)");
        System.out.println(result5);

        //Exp()

        //Float64()
        Double result6 = (Double) AviatorEvaluator.execute("double(3.14)");
        System.out.println(result6);

        //Int64()
        Long result7 = (Long) AviatorEvaluator.execute("long(100)");
        System.out.println(result7);

        //Log()
        Double result8 = (Double) AviatorEvaluator.execute("math.log(10)");
        System.out.println(result8);

        //Log10()
        Double result9 = (Double) AviatorEvaluator.execute("math.log10(10)");
        System.out.println(result9);

        //Max()
        Double result10 = (Double) AviatorEvaluator.execute("max(10,20,30.5)");
        System.out.println(result10);

        //Min()
        Double result11 = (Double) AviatorEvaluator.execute("min(10.5,20,30)");
        System.out.println(result11);

        //Pow()
        Double result12 = (Double) AviatorEvaluator.execute("math.pow(2, 3)");
        System.out.println(result12);

        //Random()
        Long result = (Long) AviatorEvaluator.execute("rand(10)");
        System.out.println(result);

        //Round()
        Long result13 = (Long) AviatorEvaluator.execute("math.round(2.3)");
        System.out.println(result13);

        //Sin()
        Double result14 = (Double) AviatorEvaluator.execute("math.sin(0.5)");
        System.out.println(result14);

        //Sqrt()
        Double result15 = (Double) AviatorEvaluator.execute("math.sqrt(4)");
        System.out.println(result15);

        //Tan()
        Double result16 = (Double) AviatorEvaluator.execute("math.tan(0.5)");
        System.out.println(result16);
        //UInt64()

        //Val()

    }
}
