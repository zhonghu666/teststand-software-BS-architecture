package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.ValueType;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.entity.StepVariable;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.impl.CacheService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GrammarCheckUtils {

    @Resource
    private CacheService cacheService;

    private static final Set<String> VALID_PREFIXES = Set.of(
            "Locals",
            "RunState",
            "FileGlobals",
            "Globals",
            "SequenceData"
    );
    private static final Set<String> VALID_BRACKETS = new HashSet<>(Arrays.asList("(", ")", "[", "]", "{", "}", ",", "'", "'"));

    private static final String[] operators = {
            "==", "!=", "<>", ">=", "<=", ">>", "<<", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "^=", "&=", "|=", "&&", "||",
            "=", "+", "-", "*", "/", "%", "^", "&", "|", "~", ">", "<", "!", "(", ")", ","
    };

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


    @Data
    private class Bracket {
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
     *
     * @param expression
     * @param result
     */
    public void hasMatchingBrackets(String expression, BracketValidationResponse result) {
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


    private boolean isMatchingPair(char opening, char closing) {
        return (opening == '(' && closing == ')') ||
                (opening == '[' && closing == ']') ||
                (opening == '{' && closing == '}');
    }

    private char getExpectedClosing(char opening) {
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

    /**
     * 获取函数列表
     *
     * @return
     */
    public List<FunctionMetadata> getFunction() {
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
        GrammarCheckUtils grammarCheckUtils = new GrammarCheckUtils();
        List<String> tokens = Arrays.asList("(", "(", "x", "+", "12", ")", "/", "23", "+", "v", "-", "(", "s", "<<", "21", ")", ")");
    }


    @Data
    private class Token {
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
     * @return
     */
    public void processExpression(String expression, StepVariable stepVariable, BracketValidationResponse response, String resultType) {
        List<Token> tokens = new ArrayList<>();
        List<FunctionMetadata> functions = getFunction();
        expression = processingExpressionArry(expression, stepVariable, response, functions, "NUMBER");
        List<String> functionNames = functions.stream().filter(i -> i.getType().equals("Functions")).map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
        Map<String, FunctionMetadata> functionMap = functions.stream().filter(i -> i.getType().equals("Functions")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        Map<String, FunctionMetadata> operatorMap = functions.stream().filter(i -> i.getType().equals("Operators")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        int index = 0;
        while (index < expression.length()) {
            int start = index;
            while (index < expression.length() && !isOperator(expression, index)) {
                index++;
            }
            if (start != index) {
                tokens.add(new Token(expression.substring(start, index), start, index - 1));
            }

            int opLength = lengthOfOperator(expression, index);
            if (opLength > 0) {
                tokens.add(new Token(expression.substring(index, index + opLength), index, index + opLength - 1));
                index += opLength;
            }

            while (index < expression.length() && expression.charAt(index) == ' ') {
                index++;  // Skip spaces
            }
        }
        legalVerify(tokens, response, functions);
        Stack<Integer> stack = new Stack<>();
        int i = 0;
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
                        replaceFunctionWithPlaceholder(tokens, funcIndex, i, functionMap, operatorMap, response, stepVariable);
                        i = funcIndex; // Reset i to the position of the new placeholder
                    }
                }
            }
            i++;
        }
        String expressionResultType;
        if (tokens.size() == 1) {
            expressionResultType = getParamType(tokens.get(0).getValue(), stepVariable);
        } else {
            List<Token> postfix = toPostfix(tokens, functionNames, operatorMap);
            expressionResultType = operatorParamVerify(operatorMap, stepVariable, response, postfix);
        }
        if ("NONE".equals(resultType) && expressionResultType != null) {
            response.setReturnErrorMsg("The window does not need to return, but return expressionResultType :" + expressionResultType);
            if (response.isValid()) {
                response.setValid(false);
            }
        } else if (!"NONE".equals(resultType)) {
            if (expressionResultType == null || !expressionResultType.equals(resultType)) {
                response.setReturnErrorMsg("The expression returned does not meet the window requirements, need:" + resultType + ", but got:" + expressionResultType);
                if (response.isValid()) {
                    response.setValid(false);
                }
            }
        }

    }

    private boolean isOperator(String expr, int index) {
        return lengthOfOperator(expr, index) > 0;
    }

    private int lengthOfOperator(String expr, int index) {
        for (String op : operators) {
            if (index + op.length() <= expr.length() && expr.substring(index, index + op.length()).equals(op)) {
                return op.length();
            }
        }
        return 0;  // No operator found at this position
    }

    /**
     * 校验表达式是否存在非法字符
     *
     * @param tokens
     * @param response
     * @param functions
     */
    private static void legalVerify(List<Token> tokens, BracketValidationResponse response, List<FunctionMetadata> functions) {
        Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?"); // Pattern for numbers (integer and floating point)
//        Pattern identifierPattern = Pattern.compile("[a-zA-Z_]\\w*"); // Pattern for valid identifiers (variable names)
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'"); // Pattern for string literals
        for (Token token : tokens) {
            String value = token.getValue();
            if (!isValidPrefix(value)
                    && functions.stream().noneMatch(i -> i.getFunctionName().equals(value)) &&
                    !numberPattern.matcher(value).matches()
                 //   && !identifierPattern.matcher(value).matches()
                    && !VALID_BRACKETS.contains(value)
                    && !stringPattern.matcher(value).matches()) {
                System.out.println(value);
                response.addError("illegal characters " + value, token.getStartPos(), token.getEndPos());
            }
        }
    }

    /**
     * 校验函数内参数个数是否匹配，入参数据类型是否匹配
     *
     * @param tokens
     * @param start
     * @param end
     * @param functionMetadataMap
     * @param operatorMap
     * @param response
     */
    private void replaceFunctionWithPlaceholder(List<Token> tokens, int start, int end,
                                                Map<String, FunctionMetadata> functionMetadataMap,
                                                Map<String, FunctionMetadata> operatorMap,
                                                BracketValidationResponse response, StepVariable stepVariable) {
        StringBuffer placeholder = new StringBuffer();
        placeholder.append("F_");
        List<List<Token>> newTokensList = new ArrayList<>(); // 最外层列表
        List<Token> currentParamTokens = new ArrayList<>();
        Token functionName = tokens.get(start);
        for (int j = start + 1; j < end; j++) {
            Token token = tokens.get(j);
            String tokenValue = token.getValue();
            // 遇到逗号，意味着一个参数的结束
            if (tokenValue.equals(",")) {
                List<Token> postfix = toPostfix(currentParamTokens, new ArrayList<>(), operatorMap);
                // 添加当前参数的token列表到最外层列表中，并准备新的参数列表
                newTokensList.add(postfix);
                currentParamTokens.clear();
            } else {
                // 如果不是逗号，则添加token到当前参数的token列表中
                currentParamTokens.add(token);
            }
        }

        // 确保最后一个参数被添加，如果有的话
        if (!currentParamTokens.isEmpty()) {
            List<Token> postfix = toPostfix(currentParamTokens, new ArrayList<>(), operatorMap);
            newTokensList.add(postfix);
        }

        // 清理已处理的tokens，并插入占位符
        for (int j = end; j >= start; j--) {
            tokens.remove(j);
        }
        System.out.println(JSON.toJSON(newTokensList));

        // Validate function parameter count
        if (functionMetadataMap.containsKey(functionName.getValue())) {
            FunctionMetadata functionMetadata = functionMetadataMap.get(functionName.getValue());
            placeholder.append(functionMetadata.getReturnType());
            int paramCount = newTokensList.size();
            if (paramCount < functionMetadata.getParamCountLow()) {
                response.addError("Number of parameters is less than minimum " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
            } else if (paramCount > functionMetadata.getParamCountHig()) {
                response.addError("Number of parameters is higher than maximum " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
            }
            processTokens(newTokensList, operatorMap, functionMetadata, stepVariable, response);
        } else {
            response.addError("Function not defined: " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
        }
        tokens.add(start, new Token(placeholder.toString(), start, start + placeholder.length()));
    }


    private void processTokens(List<List<Token>> tokens, Map<String, FunctionMetadata> operatorMap,
                               FunctionMetadata function,
                               StepVariable stepVariable, BracketValidationResponse response) {
        if (function.getParamCountLow() == 0) {
            return;
        }
        List<String> functionParamTypes = extractPatterns(function.getTemplate());
        int j = 0;
        for (List<Token> paramToken : tokens) {
            String paramType = functionParamTypes.get(j);
            String operatorResultType = null;
            if (paramToken.size() == 1) {
                operatorResultType = getParamType(paramToken.get(0).getValue(), stepVariable);
            } else {
                operatorResultType = operatorParamVerify(operatorMap, stepVariable, response, paramToken);
            }
            // 检查运算结果类型是否符合预期的参数类型
            if (operatorResultType == null) {
                response.addError("Function: " + function.getFunctionName() + ":" + (j + 1) + " parameter is null", paramToken.get(0).getStartPos(), paramToken.get(paramToken.size() - 1).getEndPos());
            } else {
                if (!paramType.equals(ValueType.OBJECT.name()) && !operatorResultType.equals(paramType)) {
                    response.addError("Function: " + function.getFunctionName() + ":" + (j + 1) + " parameter type error", paramToken.get(0).getStartPos(), paramToken.get(paramToken.size() - 1).getEndPos());
                }
            }
            j++;
        }
    }

    /**
     * 对传入后缀表达式参数列表进行 运算符参数数据类型校验
     *
     * @param operatorMap  运算符map
     * @param stepVariable 变量树
     * @param response     异常栈
     * @param paramToken   后缀表达式参数列表
     * @return 最终结果数据类型
     */
    private String operatorParamVerify(Map<String, FunctionMetadata> operatorMap, StepVariable stepVariable, BracketValidationResponse response, List<Token> paramToken) {
        int i = 0;
        String operatorResultType = null;
        Token token = paramToken.get(paramToken.size() - 1);
        while (i < paramToken.size()) {
            Token currentToken = paramToken.get(i);
            // 检查是否存在运算符，以及前面是否有两个Token作为操作数
            if (i - 2 >= 0 && operatorMap.containsKey(currentToken.getValue())) {
                Token operand1 = paramToken.get(i - 2);
                Token operand2 = paramToken.get(i - 1);
                FunctionMetadata operator = operatorMap.get(currentToken.getValue());

                // 进行类型校验或其他逻辑处理
                String chileOperator = operatorParameterVerification(operand1, currentToken, operand2, operator, stepVariable, response);

                // 处理结束后，使用占位符替换这三个Token
                String placeholder = "F_" + chileOperator; // 占位符名称可以根据需要调整
                Token placeholderToken = new Token(placeholder, operand1.getStartPos(), currentToken.getEndPos()); // 创建占位符Token

                // 将原先三个Token的位置替换为一个占位符Token
                paramToken.set(i - 2, placeholderToken); // 替换第一个操作数为占位符
                paramToken.remove(i); // 移除原运算符
                paramToken.remove(i - 1); // 移除第二个操作数，注意列表长度已变化

                // 由于列表长度减少，i需要适当调整以指向下一个正确的位置
                i = i - 2; // 合并后，i指向新的占位符位置，循环会使i自增，指向下一个待处理的Token
            } else {
                i++; // 如果当前Token不是运算符或没有足够的操作数，则移到下一个Token
            }
        }
        FunctionMetadata functionMetadata = operatorMap.get(token.getValue());
        if (functionMetadata == null) {
            log.error("operator:{} not fond", token.getValue());
            //response.addError("operator: " + token.getValue() + " not fond", token.getStartPos(), token.getEndPos());
        } else {
            operatorResultType = functionMetadata.getReturnType();
        }
        return operatorResultType;
    }


    /**
     * 校验运算符两侧参数数据类型是否合法，并返回运算结果数据类型
     *
     * @param param1        参数1
     * @param operatorParam 运算符
     * @param param2        参数2
     * @param operator      运算符信息
     * @param stepVariable  变量树
     * @param response      异常栈
     * @return 运算结果数据类型
     */
    private String operatorParameterVerification(Token param1, Token operatorParam, Token param2, FunctionMetadata operator, StepVariable stepVariable, BracketValidationResponse response) {
        String param1Type = getParamType(param1.getValue(), stepVariable);
        String param2Type = getParamType(param2.getValue(), stepVariable);
        if (param2Type == null || param1Type == null) {
            return null;
        }
        if (operator.getFunctionName().equals("+")) {
            if (!isValidType(param1Type, ValueType.STRING.name(), ValueType.NUMBER.name())) {
                response.addError("operator + param type error " + param1.getValue(), param1.getStartPos(), param1.getEndPos());
            }
            if (!isValidType(param2Type, ValueType.STRING.name(), ValueType.NUMBER.name())) {
                response.addError("operator + param type error " + param1.getValue(), param1.getStartPos(), param1.getEndPos());
            }
            if (param1Type.equals(ValueType.STRING.name()) || param2Type.equals(ValueType.STRING.name())) {
                return ValueType.STRING.name();
            } else if (param1Type.equals(ValueType.NUMBER.name()) && param2Type.equals(ValueType.NUMBER.name())) {
                return ValueType.NUMBER.name();
            }
        } else if (isValidType(operator.getFunctionType(), "Bitwise", "Arithmetic")) {
            if (!param1Type.equals(ValueType.NUMBER.name())) {
                response.addError("Operators Arithmetic and Bitwise parameter types error " + param1.getValue(), param1.getStartPos(), param1.getEndPos());
            } else if (!param2Type.equals(ValueType.NUMBER.name())) {
                response.addError("Operators Arithmetic and Bitwise parameter types error " + param2.getValue(), param2.getStartPos(), param2.getEndPos());
            }
            return ValueType.NUMBER.name();
        } else if (operator.getFunctionType().equals("Assignment")) {
            response.addError("Operators Arithmetic and Bitwise parameter types error " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
            return null;
        } else if (operator.getFunctionType().equals("Comparison")) {
            if (!param1Type.equals(param2Type)) {
                response.addError("Operators Comparison parameter types inconsistent " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
            } else {
                if (!isValidType(param1Type, ValueType.NUMBER.name(), ValueType.STRING.name(), ValueType.BOOLEAN.name())) {
                    response.addError("Operators Comparison parameter types error " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
                }
            }
            return ValueType.BOOLEAN.name();
        } else if (operator.getFunctionType().equals("Logical")) {
            if (!param1Type.equals(ValueType.BOOLEAN.name()) && !param2Type.equals(ValueType.BOOLEAN.name())) {
                response.addError("Operators Logical parameter types error " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
            }
            return ValueType.BOOLEAN.name();
        }
        return null;
    }

    /**
     * 获取变量数据类型
     *
     * @param param        变量字符串
     * @param stepVariable 变量树
     * @return
     */
    private String getParamType(String param, StepVariable stepVariable) {
        if (isValidPrefix(param)) {
            if (param.startsWith("SequenceData")) {
                // 使用正则表达式删除所有方括号及其内容以及随后的点号
                param = param.replaceAll("\\[.*?\\]\\.", "");
            }
            ValueType paramType = stepVariable.getTypeByPath(param);
            if (paramType == null) {
                return null;
            } else {
                return paramType.name();
            }
        } else if (param.startsWith("F_")) {
            return param.substring(2);
        } else if (param.matches("^'.+'$")) {
            return ValueType.STRING.name();
        } else if (StringUtils.isNumeric(param)) {
            return ValueType.NUMBER.name();
        } else if ("true".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param)) {
            return ValueType.BOOLEAN.name();
        }
        return null;
    }

    private boolean isValidType(String type, String... validTypes) {
        return Arrays.asList(validTypes).contains(type);
    }

    private String processingExpressionArry(String expression, StepVariable stepVariable, BracketValidationResponse response, List<FunctionMetadata> functions, String resultType) {
        Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher bracketMatcher = bracketPattern.matcher(expression);
        int index = 1;
        while (bracketMatcher.find()) {
            String bracketExpression = bracketMatcher.group(1);
            if (functions.stream().anyMatch(i -> bracketExpression.contains(i.getFunctionName()))) {
                processExpression(bracketExpression, stepVariable, response, resultType);
                expression = expression.replace("[" + bracketExpression + "]", "[" + index + "]");
                index++;
            }
        }
        return expression;
    }

    private List<Token> toPostfix(List<Token> tokens, List<String> functionNames, Map<String, FunctionMetadata> operatorMap) {
        List<Token> outputQueue = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : tokens) {
            String value = token.getValue();
            if (functionNames.contains(value)) {
                operatorStack.push(token);
            } else if (operatorMap.containsKey(value)) {
                FunctionMetadata functionMetadata = operatorMap.get(value);
                while (!operatorStack.isEmpty() && operatorMap.containsKey(operatorStack.peek().getValue())) {
                    String op = operatorStack.peek().getValue();
                    FunctionMetadata functionMetadata1 = operatorMap.get(op);
                    if ((functionMetadata.getOperatorAssociativity().equals("left") && functionMetadata.getOperatorPrecedence() <= functionMetadata1.getOperatorPrecedence()) ||
                            (functionMetadata.getOperatorAssociativity().equals("right") && functionMetadata.getOperatorPrecedence() < functionMetadata1.getOperatorPrecedence())) {
                        outputQueue.add(operatorStack.pop());
                    } else {
                        break;
                    }
                }
                operatorStack.push(token);
            } else if (value.equals("(")) {
                operatorStack.push(token);
            } else if (value.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().getValue().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek().getValue().equals("(")) {
                    operatorStack.pop(); // Remove "("
                }
                if (!operatorStack.isEmpty() && functionNames.contains(operatorStack.peek().getValue())) {
                    outputQueue.add(operatorStack.pop()); // Add function to the output queue
                }
            } else {
                outputQueue.add(token); // If it's a number or variable
            }
        }

        while (!operatorStack.isEmpty()) {
            Token leftOverToken = operatorStack.pop();
            if (!leftOverToken.getValue().equals("(")) {  // Ensure no leftover '(' are added to the output
                outputQueue.add(leftOverToken);
            }
        }

        return outputQueue;
    }


    public List<String> extractPatterns(String input) {
        // 正则表达式，匹配${...}格式
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);

        List<String> results = new ArrayList<>();

        while (matcher.find()) {
            // 添加匹配到的字符串（去除${和}）
            results.add(matcher.group(1));
        }

        return results;
    }
}

