package com.cetiti.expression;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.Assert;
import com.cetiti.constant.ValueType;
import com.cetiti.entity.CircularConfig;
import com.cetiti.entity.FunctionMetadata;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestSequence;
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

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(\\:[A-Za-z0-9_.]+)*");


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
                        Number resultNumber = 0;
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
                // cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
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
     * 替换表达式中的模式为对应的值。
     * 该方法将表达式中的模式替换为对应的值，并返回一个包含替换后键值对的映射。
     *
     * @param expression     待处理的表达式字符串，包含需要替换的模式
     * @param stepVariable   步骤变量，用于获取模式对应的值
     * @param cacheService   缓存服务，用于缓存数据
     * @param testSequenceId 测试序列ID，用于特定场景下的数据获取
     * @return 替换后的键值对映射
     */
    private static Map<String, Object> replacePatternsWithValues(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        List<String> tokens = new ArrayList<>();
        int index = 0;
        GrammarCheckUtils grammarCheckUtils = new GrammarCheckUtils();
        // 提取表达式中的各个片段
        while (index < expression.length()) {
            // 跳过空格和运算符
            while (index < expression.length() && (Character.isWhitespace(expression.charAt(index)) || grammarCheckUtils.isOperator(expression, index))) {
                index += grammarCheckUtils.lengthOfOperator(expression, index) > 0 ? grammarCheckUtils.lengthOfOperator(expression, index) : 1;
            }
            if (index >= expression.length()) break;
            int start = index;
            // 检查UUID模式
            Matcher uuidMatcher = UUID_PATTERN.matcher(expression.substring(index));
            if (uuidMatcher.find() && uuidMatcher.start() == 0) {
                String uuidToken = uuidMatcher.group();
                tokens.add(uuidToken);
                index += uuidToken.length();
                continue;
            }
            while (index < expression.length() && !grammarCheckUtils.isOperator(expression, index) && !Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
            if (start != index) {
                tokens.add(expression.substring(start, index));
            }
        }

        // 处理提取出的每个变量路径
        Map<String, Object> env = new HashMap<>();
        for (String token : tokens) {
            if (GrammarCheckUtils.isValidPrefix(token, false)) {
                String convertedToken = convertVariableName(token);
                // 获取变量对应的值
                Object value = getStepVariable(token, stepVariable, cacheService, testSequenceId);
                Assert.dataHandle(value != null, "参数:" + token + "值为null，无效数据");
                env.put(convertedToken, value);
            }
        }
        return env;
    }


    /**
     * 对表达式进行预处理。
     * 该方法对表达式进行预处理，处理其中的方括号表达式，并根据内容进行相应的替换或计算。
     *
     * @param expression     待处理的表达式字符串，可能包含方括号表达式
     * @param stepVariable   步骤变量，用于获取变量的值
     * @param cacheService   缓存服务，用于缓存数据
     * @param testSequenceId 测试序列ID，用于特定场景下的数据获取
     * @return 预处理后的表达式字符串
     */
    private static String preprocessingExpression(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        Pattern bracketPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher bracketMatcher = bracketPattern.matcher(expression);

        // 获取函数名称列表
        //List<String> functionName = getFunctionName(cacheService);
        List<String> functionName = Arrays.asList("GetArrayBounds", "Contains", "GetNumElements", "CalculateRelativeDistance", "math.pow", "math.abs", "math.sqrt", "Split");
        while (bracketMatcher.find()) {
            String bracketExpression = bracketMatcher.group(1);
            Object result;
            // 如果方括号内是数字，则直接使用该数字
            if (bracketExpression.matches("\\d+")) {
                result = bracketExpression;
            }
            // 如果方括号内包含函数名，则进行表达式解析执行，并获取结果
            else if (functionName.stream().anyMatch(bracketExpression::contains)) {
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
     * 转换变量名称。
     * 该方法将变量名称中的特殊字符替换为下划线，并将 UUID 中的连字符替换为下划线。
     *
     * @param variableName 待转换的变量名称字符串
     * @return 转换后的变量名称字符串
     */
    private static String convertVariableName(String variableName) {
        // 将点号替换为下划线，但不替换函数名中的点号
        String regexDots = "(?<!\\d)\\.(?!(abs|sin|cos|tan|log|log10|pow|round|asin|acos|atan|sqrt|split|\\w+)\\()(?!\\d)";
        variableName = variableName.replaceAll(regexDots, "_");
        String regexBrackets = "\\[|\\]";
        variableName = variableName.replaceAll(regexBrackets, "");
        // 正则表达式匹配 UUID 格式
        String uuidRegex = "\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b";
        Pattern uuidPattern = Pattern.compile(uuidRegex);
        Matcher matcher = uuidPattern.matcher(variableName);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String uuid = matcher.group();
            String uuidWithUnderscore = uuid.replace("-", "_");
            uuidWithUnderscore = "UUID" + uuidWithUnderscore;
            matcher.appendReplacement(result, uuidWithUnderscore);
        }
        matcher.appendTail(result);
        variableName = result.toString();
        variableName = variableName.replaceAll(":", "");
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
        step.addToListAtPath("Locals.list", s1);
        StepVariable s2 = new StepVariable();
        s2.addNestedAttribute("speed", 32, "");
        s2.addNestedAttribute("hv_lon", 120.00967773538383, "");
        s2.addNestedAttribute("hv_lat", 30.27613420982849, "");
        step.addToListAtPath("Locals.list", s2);
        StepVariable s3 = new StepVariable();
        s3.addNestedAttribute("speed", 43, "");
        s3.addNestedAttribute("hv_lon", 120.00967773538383, "");
        s3.addNestedAttribute("hv_lat", 30.27613420982849, "");
        step.addToListAtPath("Locals.list", s3);

        System.out.println(JSON.toJSON(step));
        AviatorEvaluator.addFunction(new ContainsFunction());
        AviatorEvaluator.addFunction(new CalculateRelativeDistance());
        AviatorEvaluator.addFunction(new GetNumElementsFunction());
        AviatorEvaluator.addFunction(new GetArrayBoundsFunction());
        AviatorEvaluator.addFunction(new SplitFunction());
        AviatorEvaluator.addFunction(new AscFunction());
        AviatorEvaluator.addFunction(new MaxFunction());
        String expression = "CalculateRelativeDistance(Locals.list.[GetNumElements(Locals.list)-1].hv_lat,Locals.list.[GetNumElements(Locals.list)-1].hv_lon,Locals.Data.lat,Locals.Data.lon)";
        // String expression = "Max(Locals.Data.RSI.non,Locals.num1)";
        //String expression = "Asc(Locals.Data.RSI.uuid)";
        //String expression = "Locals.Data.RSI.uuid+=1";
        //currencyExecution(expression, step, null, "121");
        //Object valueByPath = step.getValueByPath("Locals.Data.BSM.speed");
        //System.out.println(valueByPath);
        //Integer b = conditionalExecution(expression, step, null, "12");
        //System.out.println(b);
    /*    currencyExecution(expression, step, null, "1212");
        Object valueByPath = step.getValueByPath("Locals.Data.RSI.uuid");
        System.out.println(valueByPath);*/
        //splitExpression(expression);
        /*   Object execute = AviatorEvaluator.execute("1 & 1");
        System.out.println(execute);*/
        Map<String, Object> stringObjectMap = expressionParsingExecution(expression, step, null, "121");
        System.out.println(stringObjectMap);
    }

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    @FunctionalInterface
    public interface LoopCondition {
        StepVariable run();
    }

    /**
     * 解析并执行循环语句。
     * 该方法根据循环配置对象执行一个循环，直到条件表达式不再满足。
     *
     * @param circularConfig 循环配置对象，包含了循环的初始化、条件和增量表达式等信息
     * @param cacheService   缓存服务，用于获取和保存测试步骤变量
     * @param testSequenceId 测试序列ID，用于标识测试序列
     * @param loopCondition  循环条件，定义了循环的终止条件
     */
    public static void parseAndExecuteForLoop(CircularConfig circularConfig, CacheService cacheService, String testSequenceId, LoopCondition loopCondition) {
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
     * 从条件表达式中提取变量，并设置变量到脚本引擎中。
     *
     * @param conditionExpression 条件表达式，包含了需要提取的变量
     * @param stepVariable        测试步骤变量，用于获取变量值
     * @param cacheService        缓存服务，用于获取变量值
     * @param testSequenceId      测试序列ID，用于标识测试序列
     * @return 包含了设置的变量信息的映射，包括了条件表达式、变量键和变量名
     */
    private static Map<String, String> setVariablesFromCondition(String conditionExpression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\b");
        Matcher matcher = pattern.matcher(conditionExpression);
        Map<String, String> param = new HashMap<>();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            // 排除特定变量名和已经存在于引擎中的变量
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
     * 根据路径获取测试步骤变量的值。
     *
     * @param path           测试步骤变量的路径，用于指定要获取的变量
     * @param stepVariable   当前测试步骤的变量对象，用于获取变量值
     * @param cacheService   缓存服务，用于获取其他序列的变量值
     * @param testSequenceId 测试序列ID，用于标识当前测试序列
     * @param <T>            变量的类型
     * @return 指定路径下的测试步骤变量值，如果路径无效或变量不存在，则返回null
     */
    public static <T> T getStepVariable(String path, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        if (path.contains(":")) {
            // 处理路径中包含冒号的情况，用于获取其他序列的变量值
            String[] split = path.split(":");
            MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
            TestSequence otherTestSequence = mongoTemplate.findById(split[0], TestSequence.class);
            Assert.handle(otherTestSequence != null, "序列不存在");
            StepVariable other = otherTestSequence.getStepVariable();
            return other.getValueByPath(split[1]);
        } else if (path.startsWith("SequenceData")) {
            // 处理路径以"SequenceData"开头的情况，用于获取当前序列的数据调用变量值
            StepVariable sequenceData = cacheService.getStepVariable("SequenceData-" + testSequenceId);
            Assert.handle(sequenceData != null, "数据调用没有数据，请检查");
            return sequenceData.getValueByPath(path);
        } else {
            return stepVariable.getValueByPath(path);
        }
    }

    /**
     * 从缓存中获取函数名称列表，如果缓存中不存在，则从数据库中获取并保存到缓存中。
     *
     * @param cacheService 缓存服务，用于操作缓存数据
     * @return 函数名称列表
     */
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
     * @param expression     待处理的条件表达式
     * @param stepVariable   步骤变量，用于获取表达式中的变量值
     * @param cacheService   缓存服务，用于操作缓存数据
     * @param testSequenceId 测试序列ID，用于标识当前测试序列
     * @return 表达式的值
     */
    public static String getCondition(String expression, StepVariable stepVariable, CacheService cacheService, String testSequenceId) {
        expression = expression.replaceAll("\\s+", "");
        expression = preprocessingExpression(expression, stepVariable, cacheService, testSequenceId);
        Map<String, Object> env = replacePatternsWithValues(expression, stepVariable, cacheService, testSequenceId);
        expression = convertVariableName(expression);
        return env.entrySet().stream().reduce(expression, (expr, entry) -> expr.replaceAll("\\b" + entry.getKey() + "\\b", Matcher.quoteReplacement(entry.getValue().toString())), (expr1, expr2) -> expr1);
    }
}