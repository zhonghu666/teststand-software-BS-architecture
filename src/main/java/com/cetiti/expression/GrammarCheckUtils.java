package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.impl.CacheService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import utils.entity.BusinessException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GrammarCheckUtils {

    private static final Set<String> VALID_PREFIXES = Set.of(
            "Locals",
            "RunState",
            "FileGlobals",
            "Globals",
            "SequenceData"
    );
    private static final Set<String> VALID_BRACKETS = new HashSet<>(Arrays.asList("(", ")", "[", "]", "{", "}",",","'","'"));

    /**
     * 检查给字符串是否为参数。
     *
     * @param input 要检查的字符串。
     * @return 如果前缀有效，返回true；否则返回false。
     */
    public static boolean isValidPrefix(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        // 通过'.'分割字符串
        String[] parts = input.split("\\.", 2);
        // 检查是否存在至少一个分割部分，并且第一部分是否在有效前缀集合中
        return parts.length >= 1 && VALID_PREFIXES.contains(parts[0]);
    }


    private static class Bracket {
        char type;
        int position;

        Bracket(char type, int position) {
            this.type = type;
            this.position = position;
        }
    }

    /**
     * 实现括号匹配检查并返回错误括号的位置，我们可以使用一个栈（Stack）来跟踪左括号的位置，当遇到一个右括号时，我们检查栈顶的左括号是否与之匹配，并记录括号的位置。
     * 如果在处理表达式的过程中遇到无法匹配的右括号，或者在表达式结束后栈中仍然有未匹配的左括号，我们就可以确定表达式中括号的匹配是不正确的，并且返回相关的位置信息
     * 尝试访问或移除栈（Stack）顶部元素之前，检查栈是否为空。如果栈为空，这意味着遇到了一个没有对应开括号的闭括号。
     * todo 多闭括号判断时，错误括号索引会有错误。
     * @param expression
     * @param result
     */
    public static void hasMatchingBrackets(String expression, BracketValidationResponse result) {
        Stack<Bracket> stack = new Stack<>();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '(' || ch == '[') {
                stack.push(new Bracket(ch, i));
            } else if (ch == ')' || ch == ']') {
                if (stack.isEmpty()) {
                    result.addError(") Unmatched closing bracket", i, 0);
                    continue;  // 继续检查表达式，寻找其他可能的错误
                }
                Bracket top = stack.pop();
                if (!isMatchingPair(top.type, ch)) {
                    result.addError("Mismatched bracket, expected " + getExpectedClosing(top.type) + " but found " + ch, i, 0);
                }
            }
        }

        // 检查是否有未匹配的开括号
        while (!stack.isEmpty()) {
            Bracket firstUnmatched = stack.pop();
            result.addError("( Unmatched opening bracket", firstUnmatched.position, 0);
        }
    }


    private static boolean isMatchingPair(char opening, char closing) {
        return (opening == '(' && closing == ')') ||
                (opening == '[' && closing == ']') ||
                (opening == '{' && closing == '}');
    }

    private static char getExpectedClosing(char opening) {
        switch (opening) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '{':
                return '}';
            default:
                return Character.MIN_VALUE;
        }
    }

    private static List<FunctionMetadata> getFunction(CacheService cacheService) {
        List<FunctionMetadata> function = cacheService.getFunctionMetadata("Function");
        if (function == null || function.isEmpty()) {
            MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
            List<FunctionMetadata> functionMetadataList = mongoTemplate.findAll(FunctionMetadata.class);
            cacheService.saveOrUpdateFunctionMetadata("Function", functionMetadataList);
            function = cacheService.getFunctionMetadata("Function");
        }
        return function;
    }

    public static void main(String[] args) {
        String expression = "";
        BracketValidationResponse response = new BracketValidationResponse();
        hasMatchingBrackets(expression, response);
        System.out.println(JSON.toJSON(response));
    }


    @Data
    static class Token {
        String value;
        int startPos;
        int endPos;

        Token(String value, int startPos, int endPos) {
            this.value = value;
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    /**
     * 遍历分割后的表达式数组，并逐层识别和处理最内层的函数调用，直至所有的函数调用都被处理完毕。这种方法允许我们在解析过程中动态地判断函数是否含有嵌套，并在确认一个函数为最内层函数后立即进行处理和替换。
     *
     * @param expression
     * @param cacheService
     * @return
     */
    public static void processExpression(String expression, CacheService cacheService, BracketValidationResponse response) {
        List<Token> tokens = new ArrayList<>();
        List<FunctionMetadata> functions = getFunction(cacheService);
        List<String> functionNames = functions.stream().filter(i -> i.getType().equals("Functions")).map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
        Map<String, FunctionMetadata> functionMap = functions.stream().filter(i -> i.getType().equals("Functions")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        Map<String, FunctionMetadata> OperatorMap = functions.stream().filter(i -> i.getType().equals("Operators")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        int index = 0;
        int functionCounter = 0;
        while (index < expression.length()) {
            int start = index;
            while (index < expression.length() && !"+-*/=(), ".contains(expression.substring(index, index + 1))) {
                index++;
            }
            if (start != index) {
                tokens.add(new Token(expression.substring(start, index), start, index - 1));
            }
            if (index < expression.length() && "+-*/=(),".contains(expression.substring(index, index + 1))) {
                tokens.add(new Token(expression.substring(index, index + 1), index, index));
                index++;
            }
            while (index < expression.length() && " ".contains(expression.substring(index, index + 1))) {
                index++;  // Skip spaces
            }
        }
        Stack<Integer> stack = new Stack<>();
        int i = 0;
        legalVerify(tokens, response, functions);
        while (i < tokens.size()) {
            String token = tokens.get(i).getValue();
            if (functionNames.contains(token)) {
                // Push the index of the function name onto the stack
                stack.push(i);
            } else if (token.equals("(")) {
                // Push the index of '(' onto the stack
                stack.push(i);
            } else if (token.equals(")")) {
                // Process closing parenthesis
                List<Integer> tempStack = new ArrayList<>();
                while (!stack.isEmpty() && !tokens.get(stack.peek()).getValue().equals("(")) {
                    tempStack.add(stack.pop());
                }
                if (!stack.isEmpty() && tokens.get(stack.peek()).getValue().equals("(")) {
                    stack.pop(); // Pop the '('
                }
                if (!stack.isEmpty() && functionNames.contains(tokens.get(stack.peek()).getValue())) {
                    int funcIndex = stack.pop();
                    // Check if the function call is the most inner one
                    if (tempStack.isEmpty()) { // No other functions inside
                        replaceFunctionWithPlaceholder(tokens, funcIndex, i, "f" + (++functionCounter), functionMap, OperatorMap, response);
                        i = funcIndex; // Reset i to the position of the new placeholder
                    }
                }
            }
            i++;
        }
    }

    private static void legalVerify(List<Token> tokens, BracketValidationResponse response, List<FunctionMetadata> functions) {
        Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?"); // Pattern for numbers (integer and floating point)
        Pattern identifierPattern = Pattern.compile("[a-zA-Z_]\\w*"); // Pattern for valid identifiers (variable names)
        for (Token token : tokens) {
            String value = token.getValue();
            if (!isValidPrefix(value)
                    && functions.stream().noneMatch(i -> i.getFunctionName().equals(value)) &&
                    !numberPattern.matcher(value).matches()
                    && !identifierPattern.matcher(value).matches()
                    &&!VALID_BRACKETS.contains(value)) {
                System.out.println(value);
                response.addError("illegal characters " + value, token.getStartPos(), token.getEndPos());
            }
        }
    }

    private static void replaceFunctionWithPlaceholder(List<Token> tokens, int start, int end, String
            placeholder,
                                                       Map<String, FunctionMetadata> functionMetadataMap,
                                                       Map<String, FunctionMetadata> operatorMap,
                                                       BracketValidationResponse response) {
        List<Token> removed = new ArrayList<>();
        Token lastNonOperatorToken = null;
        boolean flag = false;
        int operatorCount = 0;

        for (int j = end; j >= start; j--) {
            Token token = tokens.get(j);
            if (!token.getValue().equals(",") && !token.getValue().equals("(") && !token.getValue().equals(")")) {
                if (operatorMap.containsKey(token.getValue())) {
                    flag = true;
                    // Check if current operator's operands are valid
                    if (lastNonOperatorToken == null || operatorCount >= 1) {
                        response.addError("Invalid operation" + token.getValue(), token.getStartPos(), token.getEndPos());
                    }
                    operatorCount++;
                } else {
                    lastNonOperatorToken = token;
                    operatorCount = 0;  // Reset operator count after a non-operator token
                }
                removed.add(token);
            }
            tokens.remove(j);
        }
        Collections.reverse(removed);
        System.out.println(JSON.toJSON(removed));
        Token functionName = removed.get(0);

        // Validate function parameter count
        if (functionMetadataMap.containsKey(functionName.getValue())) {
            FunctionMetadata functionMetadata = functionMetadataMap.get(functionName.getValue());
            int paramCount = flag ? 1 : removed.size() - 1;
            if (functionMetadata.getParamCount() != paramCount) {
                response.addError("Invalid parameter count for " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
            }
        } else {
            response.addError("Function not defined: " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
        }

        tokens.add(start, new Token(placeholder, removed.get(0).getStartPos(), removed.get(0).getStartPos() + 1));
    }
}

