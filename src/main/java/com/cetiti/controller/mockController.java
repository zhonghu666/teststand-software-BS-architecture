package com.cetiti.controller;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.RestPathConfig;
import com.cetiti.config.TokenManagerConfig;
import com.cetiti.entity.*;
import com.cetiti.entity.step.*;
import com.cetiti.expression.GrammarCheckUtils;
import com.cetiti.request.StepVariableDTO;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.MqttProcessingService;
import com.cetiti.service.impl.CacheService;
import com.cetiti.service.impl.TestSequenceServiceImpl;
import com.cetiti.utils.ChartUtil;
import com.cetiti.utils.RedisUtil;
import com.cetiti.utils.RestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utils.ExcelUtils;
import utils.entity.ExcelImport;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api("模拟数据")
@RestController
@RequestMapping("mock")
public class mockController {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MqttProcessingService mqttProcessingService;

    @Resource
    private CacheService cacheService;

    @Resource
    private TestSequenceServiceImpl testSequenceService;
    @Resource
    private RestUtil restUtil;

    @Resource
    private RestPathConfig restPathConfig;

    @Resource
    private TokenManagerConfig tokenManagerConfig;

    @Resource
    private GrammarCheckUtils grammarCheckUtils;
    // Operator precedence map
    private static final Map<String, Integer> operatorPrecedence = new HashMap<>();
    private static final Map<String, String> operatorAssociativity = new HashMap<>();

    static {
        operatorPrecedence.put("=", 1);
        operatorPrecedence.put("+=", 1);
        operatorPrecedence.put("-=", 1);
        operatorPrecedence.put("*=", 1);
        operatorPrecedence.put("/=", 1);
        operatorPrecedence.put("%=", 1);
        operatorPrecedence.put("^=", 1);
        operatorPrecedence.put("&=", 1);
        operatorPrecedence.put("|=", 1);
        operatorPrecedence.put("&&", 2);
        operatorPrecedence.put("||", 2);
        operatorPrecedence.put("==", 3);
        operatorPrecedence.put("!=", 3);
        operatorPrecedence.put("<", 4);
        operatorPrecedence.put("<=", 4);
        operatorPrecedence.put(">", 4);
        operatorPrecedence.put(">=", 4);
        operatorPrecedence.put("+", 5);
        operatorPrecedence.put("-", 5);
        operatorPrecedence.put("*", 6);
        operatorPrecedence.put("/", 6);
        operatorPrecedence.put("%", 6);
        operatorPrecedence.put("&", 7);
        operatorPrecedence.put("|", 7);
        operatorPrecedence.put("^", 7);
        operatorPrecedence.put("~", 7);  // Unary bitwise NOT
        operatorPrecedence.put("<<", 8);
        operatorPrecedence.put(">>", 8);
        operatorPrecedence.put("!", 9);  // Unary logical NOT
        operatorPrecedence.put("++", 10); // Unary increment
        operatorPrecedence.put("--", 10); // Unary decrement

        // Operator associativity map
        operatorAssociativity.put("=", "right");
        operatorAssociativity.put("+=", "right");
        operatorAssociativity.put("-=", "right");
        operatorAssociativity.put("*=", "right");
        operatorAssociativity.put("/=", "right");
        operatorAssociativity.put("%=", "right");
        operatorAssociativity.put("^=", "right");
        operatorAssociativity.put("&=", "right");
        operatorAssociativity.put("|=", "right");
        operatorAssociativity.put("&&", "left");
        operatorAssociativity.put("||", "left");
        operatorAssociativity.put("==", "left");
        operatorAssociativity.put("!=", "left");
        operatorAssociativity.put("<", "left");
        operatorAssociativity.put("<=", "left");
        operatorAssociativity.put(">", "left");
        operatorAssociativity.put(">=", "left");
        operatorAssociativity.put("+", "left");
        operatorAssociativity.put("-", "left");
        operatorAssociativity.put("*", "left");
        operatorAssociativity.put("/", "left");
        operatorAssociativity.put("%", "left");
        operatorAssociativity.put("&", "left");
        operatorAssociativity.put("|", "left");
        operatorAssociativity.put("^", "left");
        operatorAssociativity.put("<<", "left");
        operatorAssociativity.put(">>", "left");
        operatorAssociativity.put("!", "right");  // Unary logical NOT is right associative
        operatorAssociativity.put("++", "right"); // Unary increment is right associative
        operatorAssociativity.put("--", "right"); // Unary decrement is right associative
    }


    @PostMapping("function")
    public List<String> importFunction(@RequestPart("file") MultipartFile file) throws Exception {
        List<function> functions = ExcelUtils.readMultipartFile(file, function.class);
        for (function function : functions) {
            FunctionMetadata functionMetadata = new FunctionMetadata();
            BeanUtils.copyProperties(function, functionMetadata);
            functionMetadata.setOperatorPrecedence(operatorPrecedence.get(functionMetadata.getFunctionName()));
            functionMetadata.setOperatorAssociativity(operatorAssociativity.get(functionMetadata.getFunctionName()));
            mongoTemplate.save(functionMetadata);
        }
        List<FunctionMetadata> all = mongoTemplate.findAll(FunctionMetadata.class);
        cacheService.saveOrUpdateFunctionMetadata("Function", all);
        return functions.stream().map(function::getFunctionName).collect(Collectors.toList());
    }

    @DeleteMapping("deleteFunction")
    public Boolean deleteFunction() {
        mongoTemplate.remove(new Query(), FunctionMetadata.class);
        cacheService.deleteFunctionName("Function");
        cacheService.deleteFunctionMetadata("Function");
        return true;
    }

    @GetMapping("getFunctionName")
    public List<String> getFunctionName() {
        return cacheService.getFunctionName("Function");
    }

    public List<FunctionMetadata> getFunctionMetadata() {
        return cacheService.getFunctionMetadata("Function");
    }

    @PostMapping("stepExecute")
    public StepVariable stepExecute(@RequestParam String id) {
        StepBase byId = mongoTemplate.findById(id, StepBase.class);
        Map<String, Object> pram = new HashMap<>();
        return byId.execute(cacheService, pram);
    }

    @GetMapping("stepVariableByKey")
    public StepVariable getStepVariableByKey(@RequestParam String key) {
        return cacheService.getStepVariable(key);
    }

    @DeleteMapping("removeCache")
    public void removeCache(@RequestParam String key) {
        cacheService.deleteStepVariable(key);
    }

    @GetMapping("testDataCall")
    public StepVariable testDataCall() throws JsonProcessingException {
        String json = "{\n" +
                "\t\"results\": [{\n" +
                "\t\t\"data\": \"{\\\"msgCnt\\\":47,\\\"msgType\\\":\\\"rsm\\\",\\\"participants\\\":[{\\\"elevation\\\":0,\\\"heading\\\":0,\\\"height\\\":32,\\\"latitude\\\":39.7294293,\\\"length\\\":459,\\\"longitude\\\":116.4880825,\\\"posConfidence\\\":0,\\\"ptcId\\\":15943,\\\"ptcTimestamp\\\":1706066202236,\\\"ptcType\\\":1,\\\"sourceType\\\":0,\\\"speed\\\":0.059999999999999998,\\\"vehicleClass\\\":0,\\\"width\\\":179}],\\\"refElevation\\\":26.699999999999999,\\\"refLatitude\\\":39.730613099999999,\\\"refLongitude\\\":116.49227,\\\"rsuId\\\":\\\"R-HK0401\\\",\\\"timestamp\\\":1706066202237,\\\"uuid\\\":\\\"uuid_739208\\\"}\\n\",\n" +
                "\t\t\"esn\": \"R-HK0401\",\n" +
                "\t\t\"table\": \"rsu_rsm_his\",\n" +
                "\t\t\"timestamp\": 1706066202237\n" +
                "\t}],\n" +
                "\t\"timestamp\": 1706066205121\n" +
                "}";
        mqttProcessingService.dataCallParse("guogi/scene/auto/result/main/197e6c5c-1a19-4461-a419-f40f73bb432d", json);
        StepVariable stepVariable = cacheService.getStepVariable("SequenceData-197e6c5c-1a19-4461-a419-f40f73bb432d");
        cacheService.deleteStepVariable("SequenceData-197e6c5c-1a19-4461-a419-f40f73bb432d");
        return stepVariable;
    }

    @GetMapping("initStepVariableRequest")
    public StepVariableDTO initStepVariableRequest(@RequestParam String id) {
        TestSequence byId = mongoTemplate.findById(id, TestSequence.class);
        cacheService.saveOrUpdateStepVariable(id, byId.getStepVariable());
        return null;
    }

    @PostMapping("initStepVariable")
    public StepVariable initStepVariable(@RequestBody StepVariableDTO request) {
        return testSequenceService.convertToEntity(request);
    }

    @DeleteMapping("deleteCache")
    public void deleteCache(@RequestParam String key) {
        cacheService.deleteStepVariable(key);
        cacheService.deleteStepVariable("SequenceData-" + key);
        redisUtil.del(key + "Main");
    }

    @GetMapping("setStep")
    public void setStep(@RequestParam String id, @RequestParam String path, @RequestParam String test) {
        TestSequence testSequence = mongoTemplate.findById(id, TestSequence.class);
        StepVariable stepVariable = testSequence.getStepVariable();
        stepVariable.addNestedAttribute(path, test, "");
        testSequence.setStepVariable(stepVariable);
        mongoTemplate.save(testSequence);
    }

    @GetMapping("deleteRedis")
    public void deleteRedis(@RequestParam String key) {
        redisUtil.del(key);
    }

    @DeleteMapping("deleteRedisVague")
    public void deleteRedisVague(@RequestParam String key) {
        redisUtil.deleteKeysByPattern(key);
    }

    @GetMapping("getScanKeys")
    public List<String> getScanKeys(@RequestParam String param) {
        return redisUtil.scanKeys(param);
    }

    @GetMapping("checkIfKeyExistsWithScan")
    public Object checkIfKeyExistsWithScan(@RequestParam String key) {
        return redisUtil.checkIfKeyExistsWithScan(key);
    }


    @GetMapping("createChart")
    public void createChart(@RequestParam String testSequenceId) {
        StepVariable stepVariable = cacheService.getStepVariable(testSequenceId);
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(testSequenceId).and("stepAdditionalList").exists(true).ne(Collections.emptyList())), StepBase.class);
        List<StepAdditional> stepAdditionalList = stepBases.get(0).getStepAdditionalList();
        List<byte[]> chart = ChartUtil.chart(stepAdditionalList, stepVariable);
        int i = 0;
        for (byte[] bytes : chart) {
            // 将byte数组保存到本地文件
            try (FileOutputStream outputStream = new FileOutputStream(i + testSequenceId + ".png")) {
                outputStream.write(bytes);
                System.out.println("图表保存成功！");
            } catch (IOException e) {
                System.out.println("图表保存失败：" + e.getMessage());
            }
            i++;
        }
    }

    @GetMapping("updateStepVariable")
    public void updateStepVariable(@RequestParam(required = false) String id) {
        if (id != null) {
            TestSequence i = mongoTemplate.findById(id, TestSequence.class);
            StepVariable stepVariable = i.getStepVariable();
            StepVariable s = stepVariable.getValueByPath("RunState.SequenceFile.Data.Seq." + i.getSequenceName());
            if (s == null) {
                s = stepVariable.getValueByPath("RunState.SequenceFile.Data.Seq.");
                stepVariable.removeAttributeByPath("RunState.SequenceFile.Data.Seq");
            }
            Map<String, StepVariable.ValueWrapper<?>> attributes = s.getAttributes();
            Set<String> scopeStrings = attributes.keySet();
            for (String scopeString : scopeStrings) {
                StepVariable scopeVariable = s.getValueByPath(scopeString);
                Map<String, StepVariable.ValueWrapper<?>> attributes1 = scopeVariable.getAttributes();
                Set<String> nameStrings = attributes1.keySet();
                for (String nameString : nameStrings) {
                    s.addNestedAttribute(scopeString + "." + nameString + ".Result.Status", "", "");
                }
            }
            stepVariable.addNestedAttribute("RunState.SequenceFile.Data.Seq." + i.getSequenceName(), s, "");
            i.setStepVariable(stepVariable);
            mongoTemplate.save(i);
        } else {
            List<TestSequence> all = mongoTemplate.findAll(TestSequence.class);
            all.forEach(i -> {
                StepVariable stepVariable = i.getStepVariable();
                StepVariable s = stepVariable.getValueByPath("RunState.SequenceFile.Data.Seq." + i.getSequenceName());
                if (s == null) {
                    s = stepVariable.getValueByPath("RunState.SequenceFile.Data.Seq.");
                    stepVariable.removeAttributeByPath("RunState.SequenceFile.Data.Seq");
                }
                Map<String, StepVariable.ValueWrapper<?>> attributes = s.getAttributes();
                Set<String> scopeStrings = attributes.keySet();
                for (String scopeString : scopeStrings) {
                    StepVariable scopeVariable = s.getValueByPath(scopeString);
                    Map<String, StepVariable.ValueWrapper<?>> attributes1 = scopeVariable.getAttributes();
                    Set<String> nameStrings = attributes1.keySet();
                    for (String nameString : nameStrings) {
                        s.addNestedAttribute(scopeString + "." + nameString + ".Result.Status", "", "");
                    }
                }
                stepVariable.addNestedAttribute("RunState.SequenceFile.Data.Seq." + i.getSequenceName(), s, "");
                i.setStepVariable(stepVariable);
                mongoTemplate.save(i);
            });
        }
    }

    @PostMapping("test4")
    public void test4(@RequestBody String s, @RequestParam String id, @RequestParam String resultType) {
        BracketValidationResponse bracketValidationResponse = new BracketValidationResponse();
        StepVariable stepVariable = cacheService.getStepVariable(id);
        s = s.replace(" ", "").replaceAll("\\s+", "");
        grammarCheckUtils.processExpression(s, stepVariable, bracketValidationResponse, resultType,false );
        System.out.println(JSON.toJSON(bracketValidationResponse));
    }

    @GetMapping("mockToken")
    public String mockToken(@RequestParam Boolean status, @RequestParam String esn) {
        return tokenManagerConfig.manageToken(status, esn);
    }

    @GetMapping("getRedisByKey")
    public Object getRedisByKey(@RequestParam String key) {
        return redisUtil.get(key);
    }

    @GetMapping("mocktest1")
    public ActionStep mock(@RequestBody ActionStep dataCallStep){
        return new ActionStep();
    }

    @Data
    public static class function {

        @ExcelImport("大类")
        private String type;

        @ExcelImport("函数类型")
        private String functionType;

        @ExcelImport("函数名称")
        private String functionName;

        @ExcelImport("参数数量下限")
        private int paramCountLow;

        @ExcelImport("参数数量上限")
        private int paramCountHig;

        @ExcelImport("返回类型")
        private String returnType;

        @ExcelImport("模版")
        private String template;

        @ExcelImport("描述")
        private String desc;

        @ExcelImport("参数描述")
        private String paramDesc;

    }
}
