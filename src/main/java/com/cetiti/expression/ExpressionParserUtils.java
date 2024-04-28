package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.Assert;
import com.cetiti.constant.ValueType;
import com.cetiti.entity.CircularConfig;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.entity.StepVariable;
import com.cetiti.expression.array.ContainsFunction;
import com.cetiti.expression.array.GetArrayBoundsFunction;
import com.cetiti.expression.array.GetNumElementsFunction;
import com.cetiti.expression.numeric.AscFunction;
import com.cetiti.expression.numeric.CalculateRelativeDistance;
import com.cetiti.expression.numeric.MaxFunction;
import com.cetiti.expression.string.SplitFunction;
import com.cetiti.service.impl.CacheService;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import utils.entity.BusinessException;
import utils.entity.InvalidDataException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cetiti.constant.FlowControlType.F_BREAK;
import static com.cetiti.constant.FlowControlType.F_CONTINUE;

@Slf4j
public class ExpressionParserUtils {

    /**
     * 数值表达式执行
     *
     * @param expression
     * @param stepVariable
     * @return
     */
    public static Map<String, Object> expressionParsingExecution(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        Map<String, Object> response = new HashMap<>();
        try {
            expression = expression.replaceAll("\\s+", "");
            expression = preprocessingExpression(expression, stepVariable, cacheService, testSequenceId);
            Map<String, Object> env = replacePatternsWithValues(expression, stepVariable, cacheService, testSequenceId);
            expression = convertVariableName(expression);
            String fullExpression = env.entrySet().stream().reduce(expression, (expr, entry) -> expr.replaceAll("\\b" + entry.getKey() + "\\b", Matcher.quoteReplacement(entry.getValue().toString())), (expr1, expr2) -> expr1);
            log.info("数值表达式解析，表达式:{}", fullExpression);
            Object result = AviatorEvaluator.execute(expression, env);
            response.put("result", result);
            env.keySet().stream().filter(key -> key.startsWith("out")).forEach(i -> {
                String key = i.substring(3).replace("_", ".");
                stepVariable.addNestedAttribute(key, env.get(i), i);
            });
            return response;
        } catch (InvalidDataException e) {
            log.error("数值表达式解析，参数无效 {}", e.getMessage());
            response.put("result", 0);
            return response;
        } catch (Exception e) {
            log.error("数值表达式执行异常", e);
            throw new BusinessException("数值表达式执行异常");
        }
    }

    /**
     * 通用表达式解析
     *
     * @param expression
     * @param stepVariable
     */
    public static void currencyExecution(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        try {
            log.info("开始解析通用表达式，表达式:{}", expression);
            // 正则表达式用于匹配所有支持的操作符
            Pattern pattern = Pattern.compile("(.+?)(\\+\\+|--)|(.+?)\\s*(\\+=|-=|\\*=|/=|%=|\\^=|&=|\\|=|=)\\s*(.+)");
            expression = expression.replaceAll("\\s+", "");
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                String variablePath = matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(3).trim();
                String operator = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(4).trim();
                String expressionValue = matcher.group(2) != null ? null : matcher.group(5).trim();
                Object result = null;

                if ("++".equals(operator) || "--".equals(operator)) {
                    Object currentValue = stepVariable.getValueByPath(variablePath);
                    if (!(currentValue instanceof Number)) {
                        throw new BusinessException("自增/自减操作的变量必须是数字类型");
                    }
                    double currentNumber = ((Number) currentValue).doubleValue();
                    result = "++".equals(operator) ? currentNumber + 1 : currentNumber - 1;
                } else if (expressionValue != null) {
                    // 其他操作符的逻辑处理
                    Map<String, Object> map = expressionParsingExecution(expressionValue, stepVariable, cacheService, testSequenceId);
                    result = map.get("result");
                    Object currentValue = stepVariable.getValueByPath(variablePath);
                    if (currentValue instanceof Number && !(result instanceof List)) {
                        Number currentNumber = (Number) currentValue;
                        Number resultNumber = (Number) result;
                        if (result != null) {
                            resultNumber = (Number) result;
                        }
                        switch (operator) {
                            case "+=":
                                result = currentNumber.doubleValue() + resultNumber.doubleValue();
                                break;
                            case "-=":
                                result = currentNumber.doubleValue() - resultNumber.doubleValue();
                                break;
                            case "*=":
                                result = currentNumber.doubleValue() * resultNumber.doubleValue();
                                break;
                            case "/=":
                                result = currentNumber.doubleValue() / resultNumber.doubleValue();
                                break;
                            case "%=":
                                result = currentNumber.doubleValue() % resultNumber.doubleValue();
                                break;
                            case "^=":
                                result = Math.pow(currentNumber.doubleValue(), resultNumber.doubleValue());
                                break;
                            case "&=":
                                result = currentNumber.intValue() & resultNumber.intValue();
                                break;
                            case "|=":
                                result = currentNumber.intValue() | resultNumber.intValue();
                                break;
                            case "=":
                                break;
                            default:
                                throw new BusinessException("不支持的操作符: " + operator);
                        }
                    }
                }
                // 更新变量值
                if (ValueType.LIST.equals(stepVariable.getTypeByPath(variablePath))) {
                    stepVariable.addToListAtPathObject(variablePath, result);
                } else {
                    stepVariable.addNestedAttributeObject(variablePath, result, "");
                }
                cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
            } else {
                throw new BusinessException("表达式格式错误: " + expression);
            }
        } catch (Exception e) {
            log.error("通用表达式解析异常", e);
            throw new BusinessException("通用表达式解析异常");
        }
    }


    /**
     * 条件表达式解析
     *
     * @param expression
     * @param stepVariable
     * @return
     */
    public static Integer conditionalExecution(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        try {
            expression = expression.trim();
            expression = expression.replaceAll("\\s+", "");
            expression = preprocessingExpression(expression, stepVariable, cacheService, testSequenceId);
            Map<String, Object> env = replacePatternsWithValues(expression, stepVariable, cacheService, testSequenceId);
            expression = convertVariableName(expression);
            String fullExpression = env.entrySet().stream().reduce(expression, (expr, entry) -> expr.replaceAll("\\b" + entry.getKey() + "\\b", Matcher.quoteReplacement(entry.getValue().toString())), (expr1, expr2) -> expr1);
            log.info("条件表达式解析，表达式:{}", fullExpression);
            Boolean execute = (Boolean) AviatorEvaluator.execute(expression, env);
            return execute ? 1 : 2;
        } catch (InvalidDataException e) {
            log.error("条件表达式解析异常，参数无效 {}", e.getMessage());
            return 3;
        } catch (Exception e) {
            log.error("条件表达式解析异常", e);
            throw new BusinessException("条件表达式解析异常");
        }
    }

    /**
     * 字符串判断
     *
     * @param expression
     * @return
     */
    public static boolean stringExpression(String expression) {
        expression = expression.trim();
        if (expression.contains("like")) {
            String[] parts = expression.split("like");
            Assert.handle(parts.length == 2, "字符串表示格式错误");
            return parts[0].trim().toLowerCase().contains(parts[1].trim().toLowerCase());
        } else if (expression.contains("=")) {
            String[] parts = expression.split("=");
            Assert.handle(parts.length == 2, "字符串表示格式错误");
            return parts[0].trim().equals(parts[1].trim());
        } else {
            throw new IllegalArgumentException("Unknown operator in expression");
        }
    }

    /**
     * 表达式内变量提取并从变量树中取值
     *
     * @param expression
     * @param stepVariable
     * @param cacheService
     * @param testSequenceId
     * @return
     */
    private static Map<String, Object> replacePatternsWithValues(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        List<String> tokens = new ArrayList<>();
        int index = 0;
        while (index < expression.length()) {
            char currentChar = expression.charAt(index);
            // 检查是否是字符串字面量的开始
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
                }
            } else {
                index++;
            }
            // 跳过非标识符字符
            while (index < expression.length() && !(Character.isLetterOrDigit(expression.charAt(index)) || expression.charAt(index) == '_' || expression.charAt(index) == '\'' || expression.charAt(index) == '\"')) {
                index++;
            }
        }
        // 处理提取出的每个变量路径
        Map<String, Object> env = new HashMap<>();
        for (String token : tokens) {
            if (!token.isEmpty() && (Character.isLetter(token.charAt(0)) || token.charAt(0) == '_') && !(token.startsWith("'") && token.endsWith("'")) && !(token.startsWith("\"") && token.endsWith("\""))) {
                if (GrammarCheckUtils.isValidPrefix(token)) {
                    String convertedToken = convertVariableName(token);
                    Object value = getStepVariable(token, stepVariable, cacheService, testSequenceId);
                    Assert.dataHandle(value != null, "参数:" + token + "值为null，无效数据");
                    env.put(convertedToken, value);
                }
            }
        }
        return env;
    }


    /**
     * 表达式预处理-用于处理数组调用，解析数组索引内的结果
     *
     * @param expression
     * @param stepVariable
     * @param cacheService
     * @param testSequenceId
     * @return
     */
    private static String preprocessingExpression(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher bracketMatcher = bracketPattern.matcher(expression);
        //List<String> functionName = getFunctionName(cacheService);
        List<String> functionName = Arrays.asList("GetArrayBounds", "Contains", "GetNumElements", "CalculateRelativeDistance", "math.pow", "math.abs", "math.sqrt", "Split");
        while (bracketMatcher.find()) {
            String bracketExpression = bracketMatcher.group(1);
            Object result;
            if (bracketExpression.matches("\\d+")) {
                result = bracketExpression;
            } else if (functionName.stream().anyMatch(bracketExpression::contains)) {
                Map<String, Object> response = expressionParsingExecution(bracketExpression, stepVariable, cacheService, testSequenceId);
                result = response.get("result");
            } else {
                result = getStepVariable(bracketExpression, stepVariable, cacheService, testSequenceId);
            }
            expression = expression.replace("[" + bracketExpression + "]", "[" + result.toString() + "]");
        }
        return expression;
    }

    /**
     * 表达式处理-把变量的.换成_用于后续的解析器使用
     *
     * @param variableName
     * @return
     */
    private static String convertVariableName(String variableName) {
        String regexDots = "(?<!\\d)\\.(?!(abs|sin|cos|tan|log|log10|pow|round|asin|acos|atan|sqrt|split|\\w+)\\()(?!\\d)";
        variableName = variableName.replaceAll(regexDots, "_");
        String regexBrackets = "\\[|\\]";
        variableName = variableName.replaceAll(regexBrackets, "");
        return variableName;
    }


    public static void main(String[] args) {
        StepVariable step = new StepVariable();
        step.addNestedAttribute("Locals.Data.BSM.id", "123456", "id");
        step.addNestedAttribute("Locals.Data.BSM.status", true, "status");
        step.addNestedAttribute("Locals.Data.BSM.speed", 1233, "speed");
        step.addNestedAttribute("Locals.Data.BSM.links", Arrays.asList("2"), "links");
        step.addNestedAttribute("Locals.Data.RSI.uuid", "dasdas", "uuid");
        step.addNestedAttribute("Locals.Data.RSI.status", false, "status");
        step.addNestedAttribute("Locals.Data.RSI.xd", -321, "xd");
        step.addNestedAttribute("Locals.Data.RSI.non", Arrays.asList(1, 2, 3, 4, 5), "non");
        step.addNestedAttribute("Locals.loopIndex", 0, "loopIndex");
        step.addNestedAttribute("Locals.Data.lon", "116.48984595333", "");
        step.addNestedAttribute("Locals.Data.lat", "39.73028018521", "");
        step.addNestedAttribute("Locals.Data.speed", "31", "");
        step.addNestedAttribute("Locals.num1", "11", "");
        step.addNestedAttribute("Locals.num2", "2", "");
        step.addNestedAttribute("Locals.num3", "2", "");
        step.addNestedAttribute("RunState_LoopNumPassed", 4, "");
        step.addNestedAttribute("RunState_LoopNumIterations", 10, "");
        StepVariable s1 = new StepVariable();
        s1.addNestedAttribute("speed", 12, "");
        s1.addNestedAttribute("hv_lon", 116.48906957584057, "");
        s1.addNestedAttribute("hv_lat", 39.72840063000842, "");
        step.addToListAtPath("list", s1);
        StepVariable s2 = new StepVariable();
        s2.addNestedAttribute("speed", 32, "");
        s2.addNestedAttribute("hv_lon", 120.00967773538383, "");
        s2.addNestedAttribute("hv_lat", 30.27613420982849, "");
        step.addToListAtPath("list", s2);
        StepVariable s3 = new StepVariable();
        s3.addNestedAttribute("speed", 43, "");
        s3.addNestedAttribute("hv_lon", 120.00967773538383, "");
        s3.addNestedAttribute("hv_lat", 30.27613420982849, "");
        step.addToListAtPath("list", s3);

        System.out.println(JSON.toJSON(step));
        AviatorEvaluator.addFunction(new ContainsFunction());
        AviatorEvaluator.addFunction(new CalculateRelativeDistance());
        AviatorEvaluator.addFunction(new GetNumElementsFunction());
        AviatorEvaluator.addFunction(new GetArrayBoundsFunction());
        AviatorEvaluator.addFunction(new SplitFunction());
        AviatorEvaluator.addFunction(new AscFunction());
        AviatorEvaluator.addFunction(new MaxFunction());
       // String expression = "CalculateRelativeDistance(list.[GetNumElements(list)-1].hv_lat,list.[GetNumElements(list)-1].hv_lon,Locals.Data.lat,Locals.Data.lon)";
       // String expression = "Max(Locals.Data.RSI.non,Locals.num1)";
        //String expression = "Asc(Locals.Data.RSI.uuid)";
        String expression = "'sda' >12";
        //currencyExecution(expression, step, null, "121");
        //Object valueByPath = step.getValueByPath("Locals.Data.BSM.speed");
        //System.out.println(valueByPath);
        //Integer b = conditionalExecution(expression, step, null, "12");
        //System.out.println(b);
       /* currencyExecution(expression, step, null, "1212");
        Object valueByPath = step.getValueByPath("Locals.Data.RSI.xd");
        System.out.println(valueByPath);*/
        //splitExpression(expression);
        /*   Object execute = AviatorEvaluator.execute("1 & 1");
        System.out.println(execute);*/
        Map<String, Object> stringObjectMap = expressionParsingExecution(expression, step, null, "121");
        System.out.println(stringObjectMap);
        //List<Object> allFinalValues = step.fetchAllFinalValues();
        //System.out.println(allFinalValues);
    }

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    @FunctionalInterface
    public interface LoopCondition {
        StepVariable run();
    }

    /**
     * 循环控制 自旋循环表达式解析及执行
     *
     * @param circularConfig
     * @param cacheService
     * @param testSequenceId
     * @param loopCondition
     * @return
     * @throws ScriptException
     */
    public static void parseAndExecuteForLoop(CircularConfig circularConfig, CacheService cacheService, String testSequenceId, LoopCondition loopCondition) throws ScriptException {
        // 解析并设置初始表达式
        String[] initialParts = circularConfig.getInitializationExpression().split("=");
        String loopIndexVar = convertVariableName(initialParts[0].trim());
        int loopIndex = Integer.parseInt(initialParts[1].trim());

        // 在脚本引擎中设置初始值
        engine.put(loopIndexVar, loopIndex);
        StepVariable stepVariable = cacheService.getStepVariable(testSequenceId);
        Map<String, String> param = setVariablesFromCondition(convertVariableName(circularConfig.getWhileExpression()), stepVariable, cacheService, testSequenceId);
        stepVariable.addNestedAttribute(initialParts[0].trim(), loopIndex, "自旋索引");
        cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
        // 解析增量表达式
        String incrementOperator = circularConfig.getIncrementExpression().contains("+=") ? "+=" : circularConfig.getIncrementExpression().contains("-=") ? "-=" : circularConfig.getIncrementExpression().contains("++") ? "++" : "--";
        int incrementValue = (incrementOperator.equals("++") || incrementOperator.equals("--")) ? 1 : Integer.parseInt(circularConfig.getIncrementExpression().replaceAll("[^\\d]", ""));
        // 循环，直到条件表达式不再满足
        while (conditionalExecution(circularConfig.getWhileExpression(), stepVariable, cacheService, testSequenceId) == 1) {
            StepVariable run = loopCondition.run();
            String type = run.getValueByPath("type");
            String subType = run.getValueByPath("subType");
            Boolean flag = run.getValueByPath("FlowStatus");
            if ("N_FLOW_CONTROL".equals(type) && F_CONTINUE.name().equals(subType) && flag) {
                continue;
            } else if ("N_FLOW_CONTROL".equals(type) && F_BREAK.name().equals(subType) && flag) {
                break;
            }
            if (StringUtils.isNotBlank(param.get("key"))) {
                engine.put(param.get("key"), stepVariable.getValueByPath(param.get("variableName")));
            }
            // 根据增量表达式更新循环索引
            if (incrementOperator.equals("+=") || incrementOperator.equals("++")) {
                loopIndex += incrementValue;
            } else {
                loopIndex -= incrementValue;
            }
            stepVariable = cacheService.getStepVariable(testSequenceId);
            stepVariable.addNestedAttribute(initialParts[0].trim(), loopIndex, "自旋索引");
            cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
            engine.put(loopIndexVar, loopIndex);
        }
    }

    /**
     * 循环变量获取
     *
     * @param conditionExpression
     * @param stepVariable
     * @param cacheService
     * @param testSequenceId
     * @return
     */
    private static Map<String, String> setVariablesFromCondition(String conditionExpression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\b");
        Matcher matcher = pattern.matcher(conditionExpression);
        Map<String, String> param = new HashMap<>();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!"LoopIndex".equals(variableName) && engine.get(variableName) == null) {
                String[] parts = variableName.split("\\.");
                String key = parts[parts.length - 1];
                param.put("conditionExpression", conditionExpression.replace(variableName, key));
                engine.put(variableName, getStepVariable(variableName, stepVariable, cacheService, testSequenceId) != null ? getStepVariable(variableName, stepVariable, cacheService, testSequenceId) : 0);
                param.put("key", key);
                param.put("variableName", variableName);
            }
        }
        return param;
    }

    /**
     * 根据路径获取变量
     *
     * @param path
     * @param stepVariable
     * @param cacheService
     * @param <T>
     * @return
     */
    public static <T> T getStepVariable(String path, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        if (path.contains(":")) {
            String[] split = path.split(":");
            StepVariable other = cacheService.getStepVariable(split[0]);
            return other.getValueByPath(path);
        } else if (path.startsWith("SequenceData")) {
            StepVariable sequenceData = cacheService.getStepVariable("SequenceData-" + testSequenceId);
            Assert.handle(sequenceData != null, "数据调用没有数据，请检查");
            return sequenceData.getValueByPath(path);
        } else {
            return stepVariable.getValueByPath(path);
        }
    }

    private static List<String> getFunctionName(CacheService cacheService) {
        List<String> functionName = cacheService.getFunctionName("Function");
        if (functionName == null || functionName.isEmpty()) {
            MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
            List<FunctionMetadata> functionMetadataList = mongoTemplate.findAll(FunctionMetadata.class);
            List<String> FunctionNameList = functionMetadataList.stream().map(FunctionMetadata::getFunctionName).collect(Collectors.toList());
            cacheService.saveOrUpdateFunctionName("Function", FunctionNameList);
            functionName = cacheService.getFunctionName("Function");
        }
        return functionName;
    }

    /**
     * 解析得到实际执行表达式-用于报告展示
     *
     * @param expression
     * @param stepVariable
     * @param cacheService
     * @param testSequenceId
     * @return
     */
    public static String getCondition(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        expression = expression.replaceAll("\\s+", "");
        expression = preprocessingExpression(expression, stepVariable, cacheService, testSequenceId);
        Map<String, Object> env = replacePatternsWithValues(expression, stepVariable, cacheService, testSequenceId);
        expression = convertVariableName(expression);
        return env.entrySet().stream().reduce(expression, (expr, entry) -> expr.replaceAll("\\b" + entry.getKey() + "\\b", Matcher.quoteReplacement(entry.getValue().toString())), (expr1, expr2) -> expr1);
    }
}