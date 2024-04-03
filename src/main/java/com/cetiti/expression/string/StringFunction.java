package com.cetiti.expression.string;

import com.googlecode.aviator.AviatorEvaluator;

import java.util.Arrays;

public class StringFunction {


    public static void main(String[] args) {
        //Len()
        Long result1 = (Long) AviatorEvaluator.execute("string.length('dsfg')");
        System.out.println(result1);


        //Split
        String[] arr = (String[]) AviatorEvaluator.execute("string.split('abc,11c,123',',')");
        System.out.println(Arrays.toString(arr));
    }
}
