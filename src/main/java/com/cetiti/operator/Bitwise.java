package com.cetiti.operator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.lexer.token.OperatorType;

public class Bitwise {


    public static void main(String[] args) {
        AviatorEvaluator.getInstance().aliasOperator(OperatorType.BIT_AND, "AND");
        AviatorEvaluator.getInstance().aliasOperator(OperatorType.OR, "OR");

        System.out.println(AviatorEvaluator.execute("1==1 AND 2==3"));
        System.out.println(AviatorEvaluator.execute("true OR false"));
        System.out.println(AviatorEvaluator.execute("true && 1==1 OR false"));

        String expression = "NOT true";
        String not = expression.replace("NOT", "!");
        System.out.println(not);
        System.out.println(AviatorEvaluator.execute(not));

        String expression2 = "2 XOR 1";
        String xor = expression2.replace("XOR", "^");
        System.out.println("XOR="+xor);
        System.out.println(AviatorEvaluator.execute(xor));

        String expression3 = "2 <> 1 && 1 <> 3";
        String notE = expression3.replace("<>", "!=");
        System.out.println(notE);
        System.out.println(AviatorEvaluator.execute(notE));


    }
}
