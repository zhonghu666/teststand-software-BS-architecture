package com.cetiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cetiti.config.IMqttSender;
import com.cetiti.config.MongoConfig;
import com.cetiti.config.RestPathConfig;
import com.cetiti.constant.*;
import com.cetiti.dto.TestSequenceExecuteStatueDto;
import com.cetiti.entity.*;
import com.cetiti.entity.step.*;
import com.cetiti.expression.GrammarCheckUtils;
import com.cetiti.request.*;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.response.InformationResponse;
import com.cetiti.response.StepExecuteResponse;
import com.cetiti.response.TestSequenceResponse;
import com.cetiti.service.ReportService;
import com.cetiti.service.TestSequenceService;
import com.cetiti.utils.DateUtils;
import com.cetiti.utils.JwtToken;
import com.cetiti.utils.RedisUtil;
import com.cetiti.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cetiti.constant.ActionType.SCENE;

@Service
@Slf4j
public class TestSequenceServiceImpl implements TestSequenceService {

    @Resource(name = MongoConfig.MONGO_TEMPLATE)
    private MongoTemplate mongoTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CacheService cacheService;

    @Resource
    private ReportService reportService;

    @Resource
    private IMqttSender iMqttSender;

    @Resource
    private RestPathConfig restPathConfig;

    @Resource
    private RestUtil restUtil;

    @Resource
    private Validator validator;

    @Resource
    private HttpServletRequest httpServletRequest;

    @Override
    public String saveTestSequence(TestSequenceSaveRequest request) {
        log.info("保存序列入参:{}", JSON.toJSON(request));
        Assert.handle(request.getSequenceName().matches("^[a-zA-Z0-9]+(_[a-zA-Z0-9]+)*$"), "序列名称不符合命名规范");
        TestSequence testSequence = mongoTemplate.findById(request.getId(), TestSequence.class);
        StepVariable stepVariable = request.getStepVariable() != null ? convertToEntity(request.getStepVariable()) : new StepVariable();
        stepVariable.addToListAtPath("RunState.UpdateTime", DateUtils.date2String(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS));
        if (testSequence == null) {
            TestSequence sequenceName = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("sequenceName").is(request.getSequenceName())), TestSequence.class);
            Assert.handle(sequenceName == null, "序列名称不可重复");
            testSequence = new TestSequence();
            testSequence.setId(request.getId());
            testSequence.setSequenceName(request.getSequenceName());
            testSequence.setCreateTime(LocalDateTime.now());
            testSequence.setStepVariable(stepVariable);
        } else {
            testSequence.setSequenceName(request.getSequenceName());
            testSequence.setStepVariable(stepVariable);
            testSequence.setUpdateTime(LocalDateTime.now());
        }
        Criteria criteria = Criteria.where("testSequenceId").is(testSequence.getId());
        Query query = new Query(criteria);
        mongoTemplate.remove(query, StepBase.class);
        List<String> stepIdList = new ArrayList<>();
        boolean isFirstMainFound = false;
        for (StepBase i : request.getStepList()) {
            if ("Main".equals(i.getScope()) && !isFirstMainFound && request.getDataCallStep() != null) {
                isFirstMainFound = true;
                StepBase dataCallStep = request.getDataCallStep();
                dataCallStep.setStepType(getStepType(dataCallStep));
                mongoTemplate.save(dataCallStep); // 保存新元素到数据库
                testSequence.setDataCallId(dataCallStep.getId());
            }
            if (i.getTestSequenceId() == null) {
                i.setTestSequenceId(request.getId());
                i.setCreateTime(LocalDateTime.now());
            }
            i.setStepType(getStepType(i));
            if (i.getStepType().equals("ACTION")) {
                ActionStep actionStep = (ActionStep) i;
                if (StringUtils.isNotBlank(actionStep.getEsn())) {
                    RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getBaseApi() + "/equ/equDetail", null, "esn=" + actionStep.getEsn(), HttpMethod.GET, JwtToken.getUsername(httpServletRequest.getHeader("token")));
                    if (resultFromApi.getData() != null) {
                        String jsonString = JSON.toJSONString(resultFromApi.getData());
                        JSONObject jsonObject = JSON.parseObject(jsonString);
                        String esnName = (String) jsonObject.get("name");
                        actionStep.setDeviceName(esnName);
                    }
                }
                if (StringUtils.isNotBlank(actionStep.getSceneId())) {
                    RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getArtificial() + "/scene/getSceneInfoById", null, "id=" + actionStep.getSceneId(), HttpMethod.POST, JwtToken.getUsername(httpServletRequest.getHeader("token")));
                    if (resultFromApi.getData() != null) {
                        String jsonString = JSON.toJSONString(resultFromApi.getData());
                        JSONObject jsonObject = JSON.parseObject(jsonString);
                        actionStep.setSceneName((String) jsonObject.get("sceneName"));
                    }
                }
                if (actionStep.getActionType().equals(SCENE)) {
                    actionStep.getSceneDistributeConfigDto().getRsuSceneConfigs().forEach(x -> {
                        RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getBaseApi() + "/equ/equDetail", null, "esn=" + x.getEsn(), HttpMethod.GET, JwtToken.getUsername(httpServletRequest.getHeader("token")));
                        if (resultFromApi.getData() != null) {
                            String jsonString = JSON.toJSONString(resultFromApi.getData());
                            JSONObject jsonObject = JSON.parseObject(jsonString);
                            String esnName = (String) jsonObject.get("name");
                            x.setDeviceName(esnName);
                        }
                    });
                }
                ActionStep save = mongoTemplate.save(actionStep);
                stepIdList.add(save.getId());
            } else {
                StepBase savedElement = mongoTemplate.save(i);
                stepIdList.add(savedElement.getId());
            }
        }
        testSequence.setStepList(stepIdList);
        mongoTemplate.save(testSequence);
        return testSequence.getId();
    }

    @Override
    public String editTestSequenceName(String id, String name) {
        boolean sequenceName = mongoTemplate.exists(new Query().addCriteria(Criteria.where("sequenceName").is(name)), TestSequence.class);
        Assert.handle(!sequenceName, "名称:" + name + "重复");
        TestSequence testSequence = mongoTemplate.findById(id, TestSequence.class);
        Assert.handle(testSequence != null, "序列" + id + "不存在");
        testSequence.setSequenceName(name);
        mongoTemplate.save(testSequence);
        return id;
    }

    @Override
    public List<TestSequenceResponse> getTestSequenceAll(String token) {
        return mongoTemplate.findAll(TestSequence.class).stream().map(i -> {
            TestSequenceResponse testSequenceResponse = new TestSequenceResponse();
            testSequenceResponse.setSequenceName(i.getSequenceName());
            testSequenceResponse.setId(i.getId());
            testSequenceResponse.setStepVariable(i.getStepVariable());
            Query query = new Query().addCriteria(Criteria.where("id").in(i.getStepList())).with(Sort.by(Sort.Order.asc("createTime")));
            List<StepBase> stepBases = mongoTemplate.find(query, StepBase.class);
            for (StepBase s : stepBases) {
                if (s instanceof ActionStep) {
                    ActionStep a = (ActionStep) s;
                    if (a.getActionType().equals(SCENE)) {
                        String sceneId = a.getSceneId();
                        RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getArtificial() + "/scene/getSceneInfoById", null, "id=" + sceneId, HttpMethod.POST, token);
                        if (resultFromApi.getData() != null) {
                            String jsonString = JSON.toJSONString(resultFromApi.getData());
                            JSONObject jsonObject = JSON.parseObject(jsonString);
                            testSequenceResponse.setSiteId((String) jsonObject.get("siteId"));
                            break;
                        }
                    }
                }
            }
            testSequenceResponse.setStepList(stepBases);
            if (i.getDataCallId() != null) {
                DataCallStep dataCallStep = mongoTemplate.findById(i.getDataCallId(), DataCallStep.class);
                testSequenceResponse.setDataCallStep(dataCallStep);
            }
            Object o = redisUtil.checkIfKeyExistsWithScan(i.getId() + "execute");
            testSequenceResponse.setExecuteStatus(o != null);
            return testSequenceResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public TestSequenceResponse getTestSequenceById(String id) {
        TestSequence byId = mongoTemplate.findById(id, TestSequence.class);
        Assert.handle(byId != null, "序列不存在");
        TestSequenceResponse testSequenceResponse = new TestSequenceResponse();
        testSequenceResponse.setSequenceName(byId.getSequenceName());
        testSequenceResponse.setId(byId.getId());
        testSequenceResponse.setStepVariable(byId.getStepVariable());
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("id").in(byId.getStepList())).with(Sort.by(Sort.Order.asc("createTime"))), StepBase.class);
        testSequenceResponse.setStepList(stepBases);
        if (byId.getDataCallId() != null) {
            DataCallStep dataCallStep = mongoTemplate.findById(byId.getDataCallId(), DataCallStep.class);
            testSequenceResponse.setDataCallStep(dataCallStep);
        }
        return testSequenceResponse;
    }

    @Override
    public Boolean removeTestSequence(String id) {
        mongoTemplate.remove(new Query().addCriteria(Criteria.where("id").is(id)), TestSequence.class);
        mongoTemplate.remove(new Query().addCriteria(Criteria.where("testSequenceId").is(id)), StepBase.class);
        cacheService.deleteStepVariable(id);
        return true;
    }

    @Override
    public StepExecuteResponse execute(ExecuteRequest request) {
        StepBase step = mongoTemplate.findById(request.getId(), StepBase.class);
        Assert.handle(step != null, "步骤信息不存在");
        redisUtil.timing(step.getTestSequenceId());
        if (step.getScope().equals("Main")) {
            Object flag = redisUtil.get(step.getTestSequenceId() + "Main");
            if (flag == null) {
                StepBase dataCall = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("testSequenceId").is(step.getTestSequenceId()).and("stepType").is("DATA_CALL")), StepBase.class);
                if (dataCall != null) {
                    log.info("开始执行数据调用");
                    Map<String, Object> param = request.getParam();
                    param.put("DATA_CALL_TOPIC", "guoqi/scene/auto/main/command");
                    dataCall.execute(cacheService, param);
                }
                redisUtil.set(step.getTestSequenceId() + "Main", "1");
            }
        }
        StepVariable execute = step.execute(cacheService, request.getParam());
        StepExecuteResponse stepExecuteResponse = new StepExecuteResponse(execute);
        if (request.getStepFlag()) {
            StepVariable stepVariable = cacheService.getStepVariable(step.getTestSequenceId());
            stepVariable.addAttribute("Step", execute, "步骤结果", null);
            stepExecuteResponse.setStepVariable(stepVariable);
        } else {
            stepExecuteResponse.setStepVariable(null);
        }
        return stepExecuteResponse;
    }

    @Override
    public StepExecuteResponse singleExecute(ExecuteRequest request) {
        StepBase step = mongoTemplate.findById(request.getId(), StepBase.class);
        Assert.handle(step != null, "步骤不存在");
        TestSequence testSequence = mongoTemplate.findById(step.getTestSequenceId(), TestSequence.class);
        Assert.handle(testSequence != null, "序列不存在");
        cacheService.saveOrUpdateStepVariable(step.getTestSequenceId(), testSequence.getStepVariable());
        StepVariable execute = step.execute(cacheService, request.getParam());
        StepExecuteResponse stepExecuteResponse = new StepExecuteResponse(execute);
        cacheService.deleteStepVariable(step.getTestSequenceId());
        return stepExecuteResponse;
    }

    @Override
    public Boolean batchExecute(batchExecuteRequest request) {
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("id").in(request.getStepIdList())), StepBase.class);
        String sequenceId = null;
        for (StepBase s : stepBases) {
            if (s.getType().equals("N_MESSAGE_POPUP")) {
                continue;
            }
            sequenceId = s.getTestSequenceId();
            s.execute(cacheService, new HashMap<>());
        }
        StepVariable childStepVariable = cacheService.getStepVariable(sequenceId);
        String runStatus = childStepVariable.getValueByPath("RunState.SequenceStatus");
        StepBase stepBase = mongoTemplate.findById(request.getStepId(), StepBase.class);
        StepVariable mainStep = cacheService.getStepVariable(stepBase.getTestSequenceId());
        String stepPath = "RunState.SequenceFile.Data.Seq." + stepBase.getTestSequenceName() + "." + stepBase.getScope() + "." + stepBase.getName() + "[" + request.getStepId() + "]";
        StepVariable step;
        if (StepStatus.PASSED.getCode().equals(runStatus)) {
            step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        } else {
            step = StepVariable.RESULT_Fail(StepStatus.FAILED);
        }
        mainStep.addNestedAttribute(stepPath, step, stepBase.getName());
        cacheService.saveOrUpdateStepVariable(stepBase.getTestSequenceId(), mainStep);
        return true;
    }

    @Override
    public Boolean saveTestSequenceConfig(TestSequenceConfig testSequenceConfig) {
        testSequenceConfig.setUpdateDate(new Date());
        TestSequenceConfig save = mongoTemplate.save(testSequenceConfig);
        return true;
    }

    @Override
    public TestSequenceConfig getTestSequenceConfig() {
        TestSequenceConfig config = mongoTemplate.findOne(new Query().with(Sort.by(Sort.Direction.DESC, "_id")).limit(1), TestSequenceConfig.class);
        if (config == null) {
            config = new TestSequenceConfig(true, 100, false, true);
        }
        return config;
    }

    @Override
    public String startSequence(StartSequenceRequest request) {
        TestSequence testSequence = mongoTemplate.findById(request.getSequenceId(), TestSequence.class);
        Assert.handle(testSequence != null, "序列不存在");
        testSequence.setExceptVersion(request.getExceptVersion());
        mongoTemplate.save(testSequence);
        String username = JwtToken.getUsername(httpServletRequest.getHeader("token"));
        if (request.getIsMain()) {
            String testName = (String) redisUtil.checkIfKeyExistsWithScan(username);
            Assert.handle(testName == null, "用户正在执行序列:" + testName + "中");
            redisUtil.set(request.getExceptVersion() + ":" + username, testSequence.getSequenceName());
        }
        String testExecuteUser = (String) redisUtil.checkIfKeyExistsWithScan(request.getSequenceId() + "execute");
        Assert.handle(testExecuteUser == null, "序列" + testSequence.getSequenceName() + "被用户" + testExecuteUser + "执行中");
        redisUtil.set(request.getExceptVersion() + ":" + request.getSequenceId() + "execute", username);

        cacheService.saveOrUpdateStepVariable(request.getSequenceId(), testSequence.getStepVariable());
        TestSequenceExecuteStatueDto testSequenceExecuteStatueDto = new TestSequenceExecuteStatueDto();
        testSequenceExecuteStatueDto.setRunStatus(true);
        testSequenceExecuteStatueDto.setId(request.getSequenceId());
        testSequenceExecuteStatueDto.setExceptVersion(request.getExceptVersion());
        iMqttSender.sendToMqtt("guoqi/testSequence/execute/runStatus", JSON.toJSONString(testSequenceExecuteStatueDto));
        return request.getSequenceId();
    }

    @Override
    public String doneSequence(String mainSequenceId, String childSequenceId, String exceptVersion) {
        String reportUrl = null;
        String username = JwtToken.getUsername(httpServletRequest.getHeader("token"));
        TestSequenceConfig testSequenceConfig = getTestSequenceConfig();
        if (StringUtils.isNotBlank(mainSequenceId)) {
            if (testSequenceConfig != null && testSequenceConfig.getReportEnable()) {
                reportUrl = reportService.generateReport(mainSequenceId);
            }
            DataCallStencil dataCallStencil = new DataCallStencil();
            dataCallStencil.setTestStart(false);
            dataCallStencil.setId(mainSequenceId);
            iMqttSender.sendToMqtt("guoqi/scene/auto/main/command", JSON.toJSONString(dataCallStencil));
            redisUtil.del(exceptVersion + ":" + username);
            redisUtil.del(exceptVersion + ":" + mainSequenceId + "execute");
            TestSequenceExecuteStatueDto testSequenceExecuteStatueDto = new TestSequenceExecuteStatueDto();
            testSequenceExecuteStatueDto.setRunStatus(false);
            testSequenceExecuteStatueDto.setId(mainSequenceId);
            testSequenceExecuteStatueDto.setExceptVersion(exceptVersion);
            iMqttSender.sendToMqtt("guoqi/testSequence/execute/runStatus", JSON.toJSONString(testSequenceExecuteStatueDto));
        }
        if (StringUtils.isNotBlank(childSequenceId)) {
            String[] split = childSequenceId.split(",");
            for (String id : split) {
                DataCallStencil dataCallStencil = new DataCallStencil();
                dataCallStencil.setTestStart(false);
                dataCallStencil.setId(id);
                iMqttSender.sendToMqtt("guoqi/scene/auto/sub/command", JSON.toJSONString(dataCallStencil));
                redisUtil.del(exceptVersion + ":" + id + "execute");
                TestSequenceExecuteStatueDto testSequenceExecuteStatueDto = new TestSequenceExecuteStatueDto();
                testSequenceExecuteStatueDto.setRunStatus(false);
                testSequenceExecuteStatueDto.setId(split[0]);
                testSequenceExecuteStatueDto.setExceptVersion(exceptVersion);
                iMqttSender.sendToMqtt("guoqi/testSequence/execute/runStatus", JSON.toJSONString(testSequenceExecuteStatueDto));
            }
        }
        return reportUrl;
    }

    @Override
    public void removeCache(String testSequenceId) {
        cacheService.deleteStepVariable(testSequenceId);
        cacheService.deleteStepVariable("SequenceData-" + testSequenceId);
        redisUtil.del(testSequenceId + "Main");
        List<SequenceCallStep> sequenceCallList = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(testSequenceId).and("stepType").is("SEQUENCE_CALL")), SequenceCallStep.class);
        sequenceCallList.forEach(s -> {
            cacheService.deleteStepVariable(s.getChildTestSequenceId());
            cacheService.deleteStepVariable("SequenceData-" + s.getChildTestSequenceId());
            redisUtil.del(s.getChildTestSequenceId() + "Main");
        });

    }

    @Override
    public int checkEsn(String esn) {
        log.info("检查esn入参:{}", esn);
        List<DataCallStep> dataCallSteps = mongoTemplate.find(new Query().addCriteria(Criteria.where("stepType").is("DATA_CALL")), DataCallStep.class);
        boolean found = dataCallSteps.stream().anyMatch(i -> i.getDataCallFields().stream().anyMatch(f -> {
            String[] split = f.getOriginalPath().split("\\.", 2);
            return split[0].equals(esn);
        }));
        return found ? 1 : 0;
    }

    @Override
    public Object getSiteResponse() {
        RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getArtificial() + "/siteList", null, null, HttpMethod.GET, null);
        Assert.handle(resultFromApi.getData() != null, "场地列表为空");
        return resultFromApi.getData();
    }

    @Override
    public BaseJson validateTestSequence(String id) {
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(id)), StepBase.class);
        List<ValidationError> validationErrors = new ArrayList<>();
        for (StepBase step : stepBases) {
            Set<ConstraintViolation<StepBase>> violations = validator.validate(step);
            validationErrors.addAll(violations.stream().map(violation -> new ValidationError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage())
            ).collect(Collectors.toList()));
        }
        if (!validationErrors.isEmpty()) {
            return new BaseJson().Fail("参数错误", validationErrors);
        }
        return new BaseJson().Success("检验通过", "检验通过");
    }

    @Override
    public BracketValidationResponse checkExpressionSyntax(String expression) {
        BracketValidationResponse bracketValidationResponse = new BracketValidationResponse();
        GrammarCheckUtils.FunctionNameVerification(expression, cacheService, bracketValidationResponse);
        return bracketValidationResponse;
    }

    @Override
    public Boolean copyTestSequence(String id, String name, String newId) {
        TestSequence testSequence = mongoTemplate.findById(id, TestSequence.class);
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(id)), StepBase.class);
        testSequence.setId(newId);
        testSequence.setSequenceName(name);
        mongoTemplate.save(testSequence);
        stepBases.forEach(i -> {
            i.setTestSequenceId(newId);
            i.setCreateTime(LocalDateTime.now());
            mongoTemplate.save(i);
        });
        return true;
    }

    @Override
    public List<InformationResponse> getInformation(String id) {
        PopupStep stepBase = mongoTemplate.findById(id, PopupStep.class);
        StepVariable stepVariable = cacheService.getStepVariable(stepBase.getTestSequenceId());
        return stepBase.getInformationDisplay().stream().map(i -> {
            InformationResponse informationResponse = new InformationResponse();
            informationResponse.setKey(i.getKey());
            informationResponse.setInformationValue(stepVariable.getValueByPath(i.getInformation()));
            return informationResponse;
        }).collect(Collectors.toList());
    }


    @Override
    public StepVariable getStepStencil(String stepType, TestStepType subType) {
        return StepVariable.RESULT(stepType, subType, StepStatus.DONE, ErrorCode.SUCCESS);
    }

    // 转换方法，将DTO转换为StepVariable实体
    public StepVariable convertToEntity(StepVariableDTO dto) {
        StepVariable stepVariable = new StepVariable();
        convertChildren(stepVariable, dto.getAttributes());
        return stepVariable;
    }

    private void convertChildren(StepVariable current, Map<String, ValueWrapperDTO> childrenDTO) {
        for (Map.Entry<String, ValueWrapperDTO> entry : childrenDTO.entrySet()) {
            String key = entry.getKey();
            ValueWrapperDTO valueWrapperDTO = entry.getValue();
            Object value = valueWrapperDTO.getValue();

            if (valueWrapperDTO.getType() == ValueType.TREE_NODE) {
                // 如果是树形节点，则递归转换其子节点
                StepVariable childStepVariable = new StepVariable();
                convertChildren(childStepVariable, valueWrapperDTO.getChildren());
                current.addAttribute(key, childStepVariable, valueWrapperDTO.getDesc(), valueWrapperDTO.getInfo());
            } else if (value instanceof List) {
                // 如果类型是List，则需要迭代处理列表中的每一项
                List<Object> newList = new ArrayList<>();
                for (Object listItem : (List<?>) value) {
                    if (listItem instanceof Number || listItem instanceof String || listItem instanceof Boolean) {
                        newList.add(listItem);
                    } else {
                        StepVariable childStepVariable = new StepVariable();
                        String jsonString = JSON.toJSONString(listItem);
                        StepVariableDTO stepVariableDTO = JSONObject.parseObject(jsonString, StepVariableDTO.class);
                        convertChildren(childStepVariable, stepVariableDTO.getAttributes());
                        newList.add(childStepVariable);
                    }
                }
                // 将转换后的列表加入当前步骤变量的属性中
                current.addAttribute(key, newList, valueWrapperDTO.getDesc(), valueWrapperDTO.getInfo());
            } else {
                // 对于非树形节点和非列表的普通属性，直接加入当前步骤变量的属性中
                current.addAttribute(key, value, valueWrapperDTO.getDesc(), valueWrapperDTO.getInfo());
            }
        }
    }


    // 转换StepVariable到StepVariableDTO
    public StepVariableDTO convertToDTO(StepVariable stepVariable) {
        StepVariableDTO dto = new StepVariableDTO();
        dto.setAttributes(new HashMap<>());

        for (Map.Entry<String, StepVariable.ValueWrapper<?>> entry : stepVariable.getAttributes().entrySet()) {
            StepVariable.ValueWrapper<?> valueWrapper = entry.getValue();
            ValueWrapperDTO valueWrapperDTO = convertValueWrapperToDTO(valueWrapper);
            dto.getAttributes().put(entry.getKey(), valueWrapperDTO);
        }

        return dto;
    }

    // 转换ValueWrapper到ValueWrapperDTO
    private ValueWrapperDTO convertValueWrapperToDTO(StepVariable.ValueWrapper<?> valueWrapper) {
        ValueWrapperDTO dto = new ValueWrapperDTO();
        dto.setType(valueWrapper.getType());
        dto.setDesc(valueWrapper.getDesc());
        dto.setInfo(valueWrapper.getInfo());

        if (valueWrapper.getValue() instanceof StepVariable) {
            dto.setValue(null); // 因为我们会将这个实体的子项放入children属性
            StepVariable childStepVariable = (StepVariable) valueWrapper.getValue();
            Map<String, ValueWrapperDTO> childrenDTO = new HashMap<>();
            for (Map.Entry<String, StepVariable.ValueWrapper<?>> childEntry : childStepVariable.getAttributes().entrySet()) {
                childrenDTO.put(childEntry.getKey(), convertValueWrapperToDTO(childEntry.getValue()));
            }
            dto.setChildren(childrenDTO);
        } else {
            dto.setValue(valueWrapper.getValue()); // 这是一个叶子节点，直接赋值
        }
        return dto;
    }

    private String getStepType(StepBase stepBase) {
        if (stepBase instanceof LabelStep) {
            return "LABEL";
        } else if (stepBase instanceof WaitStep) {
            return "WAIT";
        } else if (stepBase instanceof PopupStep) {
            return "MESSAGE_POPUP";
        } else if (stepBase instanceof SequenceCallStep) {
            return "SEQUENCE_CALL";
        } else if (stepBase instanceof FlowControlStep) {
            return "FLOW_CONTROL";
        } else if (stepBase instanceof StatementStep) {
            return "STATEMENT";
        } else if (stepBase instanceof ActionStep) {
            return "ACTION";
        } else if (stepBase instanceof TestStep) {
            return "TEST";
        } else if (stepBase instanceof DataCallStep) {
            return "DATA_CALL";
        }
        return null;
    }

    public static void main(String[] args) {
        String x = "locale.num";
        String[] split = x.split(",");
        for (String s : split) {
            System.out.println(s);
        }
        System.out.println(split[0]);
    }

}
