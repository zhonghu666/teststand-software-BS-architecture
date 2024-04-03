package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.impl.CacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import utils.entity.BusinessException;

import java.util.*;
import java.util.stream.Collectors;

public class GrammarCheckUtils {

    private static final Set<String> VALID_PREFIXES = Set.of(
            "Locals",
            "RunState",
            "FileGlobals",
            "Globals",
            "SequenceData"
    );

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

    public static void FunctionNameVerification(String expression, CacheService cacheService, BracketValidationResponse response) {
        int index = 0;
        List<FunctionMetadata> function = getFunction(cacheService);
        List<String> tokens = new ArrayList<>();
        //List<String> functionName = function.stream().map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
        List<String> functionName = Arrays.asList("GetNumElements", "CalculateRelativeDistance", "math.pow", "math.abs");
        while (index < expression.length()) {
            char currentChar = expression.charAt(index);
            if (currentChar == '\'' || currentChar == '\"') {
                int start = index;
                index++; // 移动到引号之后的字符
                // 寻找匹配的闭合引号
                while (index < expression.length() && expression.charAt(index) != currentChar) {
                    index++;
                }
                // 检查是否找到闭合引号
                if (index >= expression.length()) {
                    throw new BusinessException("Unmatched quotes in expression.");
                }
                // 包括闭合引号
                tokens.add(expression.substring(start, index + 1));
                index++; // 移动到闭合引号之后的字符
            } else if (Character.isLetterOrDigit(currentChar) || currentChar == '.' || currentChar == '[' || currentChar == ']' || currentChar == '_') {
                int varStart = index;
                while (index < expression.length() && (Character.isLetterOrDigit(expression.charAt(index)) || expression.charAt(index) == '.' || expression.charAt(index) == '[' || expression.charAt(index) == ']' || expression.charAt(index) == '_')) {
                    if (expression.charAt(index) == '[') {
                        index = expression.indexOf(']', index);
                        if (index == -1) {
                            throw new BusinessException("Unmatched brackets in expression.");
                        }
                    }
                    index++;
                }
                String token = expression.substring(varStart, index);
                if (!token.isEmpty()) {
                    tokens.add(token);
                    if (!StringUtils.isNumeric(token) && !isValidPrefix(token)) {
                        if (functionName.stream().noneMatch(token::contains)) {
                            response.addError(token, varStart, index);
                        }
                    }
                }
            } else {
                index++;
            }
            // 跳过非标识符字符
            while (index < expression.length() && !(Character.isLetterOrDigit(expression.charAt(index)) || expression.charAt(index) == '_' || expression.charAt(index) == '\'' || expression.charAt(index) == '\"')) {
                index++;
            }
        }

        // 用于存储当前函数名称和参数列表
        String currentFunction = null;
        List<String> currentParams = new ArrayList<>();
        Map<String, FunctionMetadata> expectedParamCounts = function.stream().collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        for (String token : tokens) {
            if (functionName.contains(token)) {
                // 如果找到新的函数名称，先处理上一个函数的参数
                if (currentFunction != null) {
                    // 检查参数个数是否符合预期
                    FunctionMetadata functionMetadata = expectedParamCounts.get(currentFunction);
                    if (currentParams.size() != functionMetadata.getParamCount()) {
                        throw new BusinessException("Incorrect number of parameters for function " + currentFunction);
                    }
                    // TODO: 根据expectedParamTypes验证每个参数的类型

                    // 清空参数列表，准备下一个函数的参数收集
                    currentParams.clear();
                }
                // 更新当前处理的函数名称
                currentFunction = token;
            } else if (currentFunction != null) {
                // 当前token不是函数名称，将其作为参数添加到当前函数的参数列表中
                currentParams.add(token);
            }
        }

        // 处理最后一个函数的参数
        if (currentFunction != null && !currentParams.isEmpty()) {
            // 检查参数个数是否符合预期
            if (currentParams.size() != expectedParamCounts.get(currentFunction).getParamCount()) {
                throw new BusinessException("Incorrect number of parameters for function " + currentFunction);
            }
            // TODO: 根据expectedParamTypes验证每个参数的类型
        }
    }

    private static class Bracket {
        char type;
        int position;

        Bracket(char type, int position) {
            this.type = type;
            this.position = position;
        }
    }

    public static void hasMatchingBrackets(String expression, BracketValidationResponse result) {
        Stack<Bracket> stack = new Stack<>();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == '(' || ch == '[') {
                stack.push(new Bracket(ch, i));
            } else if (ch == ')' || ch == ']') {
                if (stack.isEmpty()) {
                    result.addError("Unmatched closing bracket", i, null);
                }
                Bracket top = stack.pop();
                if (!isMatchingPair(top.type, ch)) {
                    result.addError("Mismatched bracket, expected " + getExpectedClosing(top.type) + " but found " + ch, i, null);
                }
            }
        }
        // 表达式遍历完成后，检查是否有未匹配的开括号
        if (!stack.isEmpty()) {
            Bracket lastUnmatched = stack.pop();
            result.addError("Unmatched opening bracket", lastUnmatched.position, null);
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
        String expression = "(math.abs(math.sqrt(math.pow(Locals.data.[RunState.LoopIndex].northSpeed,2)+\n" +
                "math.pow(Locals.data.[RunState.LoopIndex].eastSpeed,2))))*Locals.speed";
        BracketValidationResponse response = new BracketValidationResponse();
        //FunctionNameVerification(expression, null, response);
        hasMatchingBrackets(expression, response);
        System.out.println(JSON.toJSON(response));

    }

}
