package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.ValueType;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestSequence;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.impl.CacheService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GrammarCheckUtils {

    @Resource
    private CacheService cacheService;

    @Resource(name = MongoConfig.MONGO_TEMPLATE)
    private MongoTemplate mongoTemplate;

    /**
     * 变量命名起始
     */
    private static final Set<String> VALID_PREFIXES = Set.of(
            "Locals",
            "RunState",
            "FileGlobals",
            "Globals",
            "SequenceData"
    );
    private static final Set<String> VALID_BRACKETS = new HashSet<>(Arrays.asList("(", ")", "[", "]", "{", "}", ",", "'", "'"));

    public static final String[] operators = {
            "==", "!=", "<>", ">=", "<=", ">>", "<<", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "^=", "&=", "|=", "&&", "||",
            "=", "+", "-", "*", "/", "%", "^", "&", "|", "~", ">", "<", "!", "(", ")", ","
    };

    /**
     * 检查从指定位置开始的字符是否为运算符。
     * 该方法用于确定表达式中从指定位置开始的字符是否为有效的运算符。
     *
     * @param expr  表达式字符串
     * @param index 开始检查的位置索引
     * @return 如果从指定位置开始存在有效的运算符，则返回 true；否则返回 false。
     */
    public boolean isOperator(String expr, int index) {
        return lengthOfOperator(expr, index) > 0;
    }


    /**
     * 计算从指定位置开始的运算符的长度。
     * 该方法用于查找在表达式中从指定位置开始的运算符，并返回其长度。
     *
     * @param expr  表达式字符串
     * @param index 开始搜索的位置索引
     * @return 若从指定位置开始存在运算符，则返回该运算符的长度；若不存在运算符，则返回0。
     */
    public int lengthOfOperator(String expr, int index) {
        for (String op : operators) {
            if (index + op.length() <= expr.length() && expr.substring(index, index + op.length()).equals(op)) {
                return op.length();
            }
        }
        return 0;  // 在此位置未找到运算符
    }

    /**
     * 检查给字符串是否为参数。
     *
     * @param input 要检查的字符串。
     * @return 如果前缀有效，返回true；否则返回false。
     */
    public static boolean isValidPrefix(String input, Boolean flag) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (flag) {
            if (input.contains(":")) {
                String regex = ":[^\\.]+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    String afterColon = matcher.group(0).substring(1); // 去除匹配结果中的冒号，获取冒号后到第一个点号之间的内容
                    return VALID_PREFIXES.contains(afterColon);
                } else {
                    return false;
                }
            } else {
                // 通过'.'分割字符串
                String[] parts = input.split("\\.", 2);
                // 检查是否存在至少一个分割部分，并且第一部分是否在有效前缀集合中
                return parts.length >= 1 && VALID_PREFIXES.contains(parts[0]);
            }
        } else {
            Pattern stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'"); // Pattern for string literals
            boolean isString = stringPattern.matcher(input).matches();
            return !isString;
        }
    }


    /**
     * 临时栈
     */
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


    /**
     * 检查两个括号字符是否匹配。
     * 这个方法主要用于验证一对括号字符是否为正确的开闭组合。
     *
     * @param opening 开括号字符
     * @param closing 闭括号字符
     * @return 如果开括号与闭括号匹配（如 '(' 和 ')'），则返回 true；否则返回 false。
     */
    private boolean isMatchingPair(char opening, char closing) {
        return (opening == '(' && closing == ')') ||
                (opening == '[' && closing == ']') ||
                (opening == '{' && closing == '}');
    }


    /**
     * 根据开括号返回对应的闭括号
     *
     * @param opening 开括号
     * @return
     */
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
        List<String> myList = new ArrayList<>();
        myList.add("1");
        Class<?> listClass = myList.getClass();
        TypeVariable<?>[] typeParameters = listClass.getTypeParameters();
        System.out.println(typeParameters[0].getName()); // Output: E

    }


    /**
     * 表达式解析内容临时存储对象
     */
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
     * @param expression   表达式
     * @param stepVariable 变量树
     * @param response     异常栈
     * @param resultType   返回类型
     * @param flag
     */
    public void processExpression(String expression, StepVariable stepVariable, BracketValidationResponse response, String resultType, Boolean flag) {
        List<Token> tokens = new ArrayList<>();
        List<FunctionMetadata> functions = getFunction();
        expression = processingExpressionArry(expression, stepVariable, response, functions, "NUMBER", flag);
        List<String> functionNames = functions.stream().filter(i -> i.getType().equals("Functions")).map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
        Map<String, FunctionMetadata> functionMap = functions.stream().filter(i -> i.getType().equals("Functions")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        Map<String, FunctionMetadata> operatorMap = functions.stream().filter(i -> i.getType().equals("Operators")).collect(Collectors.toMap(FunctionMetadata::getFunctionName, a -> a));
        int index = 0;
        // Regex to recognize UUID followed by optional colon and identifiers
        Pattern uuidPattern = Pattern.compile(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(:[A-Za-z0-9_.]+)*");

        while (index < expression.length()) {
            int start = index;

            // Check if current part matches a UUID pattern
            Matcher uuidMatcher = uuidPattern.matcher(expression.substring(index));
            if (uuidMatcher.find() && uuidMatcher.start() == 0) {
                String uuidToken = uuidMatcher.group();
                tokens.add(new Token(uuidToken, index, index + uuidToken.length() - 1));
                index += uuidToken.length();
                continue;
            }

            // Check for operators
            if (isOperator(expression, index)) {
                int opLength = lengthOfOperator(expression, index);
                tokens.add(new Token(expression.substring(index, index + opLength), index, index + opLength - 1));
                index += opLength;
            } else {
                // Handle identifiers or literals
                start = index;
                while (index < expression.length() && !isOperator(expression, index)) {
                    index++;
                }
                if (start != index) {
                    tokens.add(new Token(expression.substring(start, index), start, index - 1));
                }
            }
        }
        System.out.println(JSON.toJSON(tokens));
        legalVerify(tokens, response, functions, flag);
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
                        replaceFunctionWithPlaceholder(tokens, funcIndex, i, functionMap, operatorMap, response, stepVariable,flag);
                        i = funcIndex; // Reset i to the position of the new placeholder
                    }
                }
            }
            i++;
        }
        //判断表达式最终结果是否和入参要求一致
        String expressionResultType;
        if (tokens.size() == 1) {
            expressionResultType = getParamType(tokens.get(0).getValue(), stepVariable, flag);
        } else {
            List<Token> postfix = toPostfix(tokens, functionNames, operatorMap);
            expressionResultType = operatorParamVerify(operatorMap, stepVariable, response, postfix, flag);
        }
        if (ValueType.NONE.name().equals(resultType) && !(expressionResultType == null || ValueType.NONE.name().equals(expressionResultType))) {
            response.setReturnErrorMsg("The window does not need to return, but return expressionResultType :" + expressionResultType);
            if (response.isValid()) {
                response.setValid(false);
            }
        } else if (!ValueType.NONE.name().equals(resultType)) {
            if (expressionResultType == null || !expressionResultType.equals(resultType)) {
                response.setReturnErrorMsg("The expression returned does not meet the window requirements, need:" + resultType + ", but got:" + expressionResultType);
                if (response.isValid()) {
                    response.setValid(false);
                }
            }
        }
    }


    /**
     * 校验表达式是否存在非法字符
     *
     * @param tokens
     * @param response
     * @param functions
     */
    private static void legalVerify(List<Token> tokens, BracketValidationResponse response, List<FunctionMetadata> functions, Boolean flag) {
        Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?"); // Pattern for numbers (integer and floating point)
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'"); // Pattern for string literals
        for (Token token : tokens) {
            String value = token.getValue();
            boolean isBoolean = value.equals("true") || value.equals("false");
            boolean isString = stringPattern.matcher(value).matches();
            if (!isValidPrefix(value, flag)
                    && functions.stream().noneMatch(i -> i.getFunctionName().equals(value)) &&
                    !numberPattern.matcher(value).matches()
                    && !VALID_BRACKETS.contains(value)
                    && !(isString || isBoolean)) { // Check if string but not boolean
                System.out.println(value);
                response.addError("illegal characters " + value, token.getStartPos(), token.getEndPos());
            }
        }
    }

    /**
     * 将函数替换为占位符。
     * 该方法将给定范围内的函数替换为占位符，并处理函数参数，并将处理后的参数转换为逆波兰表达式。
     *
     * @param tokens              表达式的标记列表
     * @param start               替换范围的起始索引（包含）
     * @param end                 替换范围的结束索引（不包含）
     * @param functionMetadataMap 函数元数据映射，用于验证函数参数数量和类型
     * @param operatorMap         运算符映射，用于处理函数参数转换为逆波兰表达式时的运算符优先级
     * @param response            用于记录替换过程中的错误信息和验证结果的响应对象
     * @param stepVariable        步骤变量，用于函数执行过程中的参数传递和结果存储
     */
    private void replaceFunctionWithPlaceholder(List<Token> tokens, int start, int end,
                                                Map<String, FunctionMetadata> functionMetadataMap,
                                                Map<String, FunctionMetadata> operatorMap,
                                                BracketValidationResponse response, StepVariable stepVariable,
                                                boolean flag) {
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
            processTokens(newTokensList, operatorMap, functionMetadata, stepVariable, response, flag);
        } else {
            response.addError("Function not defined: " + functionName.getValue(), functionName.getStartPos(), functionName.getEndPos());
        }
        tokens.add(start, new Token(placeholder.toString(), start, start + placeholder.length()));
    }

    /**
     * 处理函数参数的类型验证。
     * 该方法用于验证函数的参数类型是否符合预期，并记录验证结果到响应对象中。
     *
     * @param tokens       待处理的函数参数列表，每个参数是一个标记列表
     * @param operatorMap  运算符映射，用于检查运算结果类型
     * @param function     函数元数据，包含函数的模板和参数类型信息
     * @param stepVariable 步骤变量，用于获取参数的类型信息
     * @param response     用于记录验证结果的响应对象
     */
    private void processTokens(List<List<Token>> tokens, Map<String, FunctionMetadata> operatorMap,
                               FunctionMetadata function,
                               StepVariable stepVariable,
                               BracketValidationResponse response, boolean flag) {
        if (function.getParamCountLow() == 0) {
            return;
        }
        List<String> functionParamTypes = extractPatterns(function.getTemplate());
        int j = 0;
        for (List<Token> paramToken : tokens) {
            String paramType = functionParamTypes.get(j);
            String operatorResultType;
            // Determine the result type of the operator for single-token parameters
            if (paramToken.size() == 1) {
                operatorResultType = getParamType(paramToken.get(0).getValue(), stepVariable, flag);
            } else {
                // Validate parameter type for multi-token parameters
                operatorResultType = operatorParamVerify(operatorMap, stepVariable, response, paramToken, flag);
            }
            // Check if the operator result type matches the expected parameter type
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
    private String operatorParamVerify(Map<String, FunctionMetadata> operatorMap, StepVariable stepVariable, BracketValidationResponse response, List<Token> paramToken, boolean flag) {
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
                String chileOperator = operatorParameterVerification(operand1, currentToken, operand2, operator, stepVariable, response, flag);

                // 处理结束后，使用占位符替换这三个Token
                String placeholder = "F_" + chileOperator;
                Token placeholderToken = new Token(placeholder, operand1.getStartPos(), currentToken.getEndPos());

                // 将原先三个Token的位置替换为一个占位符Token
                paramToken.set(i - 2, placeholderToken);
                paramToken.remove(i);
                paramToken.remove(i - 1);
                // 由于列表长度减少，i需要适当调整以指向下一个正确的位置
                i = i - 2;
            } else {
                i++;
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
    private String operatorParameterVerification(Token param1, Token operatorParam, Token param2, FunctionMetadata operator, StepVariable stepVariable, BracketValidationResponse response, boolean flag) {
        String param1Type = getParamType(param1.getValue(), stepVariable, flag);
        String param2Type = getParamType(param2.getValue(), stepVariable, flag);
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
            if (!isValidPrefix(param1.getValue(), flag)) {
                response.addError("Operators Assignment must assign values to variables " + param1.getValue(), param1.getStartPos(), param1.getEndPos());
            } else if (operatorParam.getValue().equals("=")) {
                if (!ValueType.LIST.name().equals(param1Type) && !param1Type.equals(param2Type)) {
                    response.addError("Operators = parameter types must be consistent " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
                }
            } else {
                if (!ValueType.NUMBER.name().equals(param1Type) || !ValueType.NUMBER.name().equals(param2Type)) {
                    response.addError("Operators Assignment parameter types must be NUMBER " + operatorParam.getValue(), operatorParam.getStartPos(), operatorParam.getEndPos());
                }
            }
            return ValueType.NONE.name();
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
    private String getParamType(String param, StepVariable stepVariable, boolean flag) {
        if (isValidPrefix(param, flag)) {
            if (param.equals("SequenceData")) {
                return ValueType.LIST.name();
            }
            if (param.contains(":")) {
                String[] parts = param.split(":", 2);
                TestSequence otherTestSequence = mongoTemplate.findById(parts[0], TestSequence.class);
                ValueType typeByPath = otherTestSequence.getStepVariable().getTypeByPath(parts[1]);
                return typeByPath != null ? typeByPath.name() : null;
            }
            if (param.startsWith("SequenceData")) {
                // 使用正则表达式删除所有方括号及其内容以及随后的点号
                param = param.replaceAll("\\[.*?\\]\\.", "");
            } else {
                param = param.replaceAll("\\[.*\\]", "[0]");
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

    /**
     * 判断type是否在后面list内
     *
     * @param type
     * @param validTypes
     * @return
     */
    private boolean isValidType(String type, String... validTypes) {
        return Arrays.asList(validTypes).contains(type);
    }

    /**
     * 处理表达式中的数组表达式。
     * 该方法用于处理表达式中的数组表达式，并对其中包含的函数进行处理，然后将数组表达式替换为索引号。
     *
     * @param expression   待处理的表达式字符串
     * @param stepVariable 步骤变量，用于表达式处理过程中的参数传递和结果存储
     * @param response     用于记录处理过程中的错误信息和验证结果的响应对象
     * @param functions    函数列表，用于检查数组表达式中是否包含函数调用
     * @param resultType   表达式的结果类型
     * @param flag
     * @return 处理后的表达式字符串
     */
    private String processingExpressionArry(String expression, StepVariable stepVariable, BracketValidationResponse response, List<FunctionMetadata> functions, String resultType, Boolean flag) {
        // 匹配数组表达式的正则表达式模式
        Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher bracketMatcher = bracketPattern.matcher(expression);
        int index = 1;
        while (bracketMatcher.find()) {
            String bracketExpression = bracketMatcher.group(1);
            if (functions.stream().anyMatch(i -> bracketExpression.contains(i.getFunctionName()))) {
                // 处理数组表达式中的函数调用
                processExpression(bracketExpression, stepVariable, response, resultType, flag);
                expression = expression.replace("[" + bracketExpression + "]", "[" + index + "]");
                index++;
            }
        }
        return expression;
    }


    /**
     * 将中缀表达式转换为后缀表达式。
     * 该方法将给定的中缀表达式转换为后缀表达式，并返回后缀表达式的标记列表。
     *
     * @param tokens        中缀表达式的标记列表
     * @param functionNames 包含函数名称的列表，用于识别函数调用
     * @param operatorMap   运算符映射，用于确定运算符的优先级和结合性
     * @return 后缀表达式的标记列表
     */
    private List<Token> toPostfix(List<Token> tokens, List<String> functionNames, Map<String, FunctionMetadata> operatorMap) {
        List<Token> outputQueue = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : tokens) {
            String value = token.getValue();
            // 如果是函数名，则入栈
            if (functionNames.contains(value)) {
                operatorStack.push(token);
            }
            // 如果是运算符
            else if (operatorMap.containsKey(value)) {
                FunctionMetadata functionMetadata = operatorMap.get(value);
                // 处理运算符栈中的运算符
                while (!operatorStack.isEmpty() && operatorMap.containsKey(operatorStack.peek().getValue())) {
                    String op = operatorStack.peek().getValue();
                    FunctionMetadata functionMetadata1 = operatorMap.get(op);
                    // 根据运算符的优先级和结合性判断是否出栈
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
                    operatorStack.pop();
                }
                // 如果左括号后面是函数名，则将函数名加入到输出队列中
                if (!operatorStack.isEmpty() && functionNames.contains(operatorStack.peek().getValue())) {
                    outputQueue.add(operatorStack.pop());
                }
            } else {
                outputQueue.add(token); // If it's a number or variable
            }
        }
        // 处理剩余的运算符，并加入到输出队列中
        while (!operatorStack.isEmpty()) {
            Token leftOverToken = operatorStack.pop();
            if (!leftOverToken.getValue().equals("(")) {
                outputQueue.add(leftOverToken);
            }
        }
        return outputQueue;
    }


    /**
     * 从输入字符串中提取模式。
     * 该方法从输入字符串中提取形如 ${...} 格式的模式，并返回模式列表。
     *
     * @param input 输入字符串，包含需要提取模式的内容
     * @return 提取到的模式列表
     */
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

