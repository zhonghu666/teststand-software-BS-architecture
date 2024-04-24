package com.cetiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cetiti.config.IMqttSender;
import com.cetiti.entity.DataCallField;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.step.DataCallStep;
import com.cetiti.entity.step.StepBase;
import com.cetiti.request.CustomSignalFieldRequest;
import com.cetiti.request.CustomSignalParesRequest;
import com.cetiti.service.MqttProcessingService;
import com.cetiti.utils.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.CollectionUtils;
import utils.entity.InvalidDataException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;

@Service
@Slf4j
public class MqttProcessingServiceImpl implements MqttProcessingService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private CacheService cacheService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private IMqttSender iMqttSender;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Pattern pattern = Pattern.compile("(.+)\\[(.+)=(.+)]");

    private final Map<String, CompletableFuture<Boolean>> responseMap = new ConcurrentHashMap<>();


    @Override
    public void dataCallParse(String topic, String msg) {
        long startTime = System.currentTimeMillis();
        log.info("数据调用消息入参:msg={}", msg);
        String[] split = topic.split("/");
        DataCallStep dataCallStep = (DataCallStep) mongoTemplate.findOne(new Query().addCriteria(Criteria.where("testSequenceId").is(split[5]).and("stepType").is("DATA_CALL")), StepBase.class);
        StepVariable stepVariable = cacheService.getStepVariable("SequenceData-" + split[5]) != null ? cacheService.getStepVariable("SequenceData-" + split[5]) : new StepVariable();
        if (dataCallStep == null) {
            log.error("序列:{}数据调用步骤不存在:", split[5]);
            return;
        }
        List<DataCallField> dataCallFields = dataCallStep.getDataCallFields();
        try {
            JsonNode rootNode = mapper.readTree(msg);
            Long timestamp = Long.valueOf(rootNode.get("timestamp").asText());
            StepVariable data = new StepVariable();
            data.addNestedAttribute("timestamp", timestamp, "时间");
            for (DataCallField dataCallField : dataCallFields) {
                String[] prefix = dataCallField.getOriginalPath().split(":");
                String[] keyParts = prefix[0].split("\\.");
                String esn = keyParts[0];
                String name = keyParts[1];
                JsonNode dataNode = findDataNode(rootNode.get("results"), esn, name);
                if (dataNode != null) {
                    String newPath = dataCallField.getNewPath();
                    JsonNode currentNode = dataNode;
                    String[] pathParts = prefix[1].split("\\.");
                    List<Object> finalResults = new ArrayList<>();
                    for (String part : pathParts) {
                        if (part.contains("[")) {
                            Matcher matcher = pattern.matcher(part);
                            if (matcher.find()) {
                                String arrayName = matcher.group(1);
                                String filterField = matcher.group(2);
                                String filterValue = matcher.group(3);
                                currentNode = currentNode.get(arrayName);
                                if (filterValue.equals("-1")) {
                                    // 遍历整个数组
                                    for (JsonNode element : currentNode) {
                                        JsonNode childNode = extractNestedField(element, Arrays.copyOfRange(pathParts, Arrays.asList(pathParts).indexOf(part) + 1, pathParts.length));
                                        if (childNode != null && childNode.isValueNode()) {
                                            finalResults.add(childNode.asText());
                                        }
                                    }
                                    break;
                                } else {
                                    // 根据 filterValue 过滤数组
                                    currentNode = filterArray(currentNode, filterField, filterValue);
                                    if (currentNode == null) {
                                        log.info("array筛选条件异常，结果为空，条件={}", filterField + "=" + filterValue);
                                        break;
                                    }
                                }
                            }
                        } else {
                            currentNode = currentNode.get(part);
                        }
                    }
                    if (currentNode != null && !currentNode.isArray()) {
                        Object finalValue = isFloatNumber(currentNode.asText()) ? Double.valueOf(currentNode.asText()) : currentNode.asText();
                        data.addNestedAttribute(newPath, finalValue, dataCallField.getType().name());
                    } else if (!finalResults.isEmpty()) {
                        data.addNestedAttribute(newPath, finalResults, dataCallField.getType().name());
                    }
                }
            }
            stepVariable.addToListAtPath("SequenceData", data);
            cacheService.saveOrUpdateStepVariable("SequenceData-" + split[5], stepVariable);
            log.info("解析耗时:{}", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("解析数据调用异常:", e);
        }
    }

    @Override
    public Boolean sceneDistributeResult(String topic, String msg) {
        JSONObject jsonObject = JSON.parseObject(msg);
        String uuid = (String) jsonObject.get("uuid");
        Boolean success = (Boolean) jsonObject.get("success");
        CompletableFuture<Boolean> future = responseMap.get(uuid);
        if (future != null) {
            future.complete(success);
        }
        return null;
    }

    @Override
    public Boolean waitForResponse(String uuid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        responseMap.put(uuid, future);
        try {
            // 直接返回future结果，这里future完成时将直接返回布尔状态
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false; // 线程中断处理
        } catch (ExecutionException | TimeoutException e) {
            return false; // 执行异常或超时处理
        } finally {
            responseMap.remove(uuid);
        }
    }

    @Override
    public void startCustomSignal(String topic, String msg) {
        long startTime = System.currentTimeMillis();
        log.info("自定义信号消息入参:msg={}", msg);
        String uuid = StringUtils.substringAfterLast(topic, "/");
        List<CustomSignalFieldRequest> customSignalFieldRequests = (List<CustomSignalFieldRequest>) redisUtil.get(uuid + "startCustomSignal");
        if (CollectionUtils.isEmpty(customSignalFieldRequests)) {
            return;
        }
        try {
            JsonNode rootNode = mapper.readTree(msg);
            Long timestamp = Long.valueOf(rootNode.get("timestamp").asText());
            Map<String, Object> map = new HashMap<>();
            map.put("timestamp", timestamp);
            for (CustomSignalFieldRequest dataCallField : customSignalFieldRequests) {
                String[] prefix = dataCallField.getOriginalPath().split(":");
                String[] keyParts = prefix[0].split("\\.");
                String esn = keyParts[0];
                String name = keyParts[1];
                JsonNode dataNode = findDataNode(rootNode.get("results"), esn, name);
                if (dataNode != null) {
                    String newPath = dataCallField.getNewPath();
                    JsonNode currentNode = dataNode;
                    String[] pathParts = prefix[1].split("\\.");
                    List<Object> finalResults = new ArrayList<>();
                    for (String part : pathParts) {
                        if (part.contains("[")) {
                            Matcher matcher = pattern.matcher(part);
                            if (matcher.find()) {
                                String arrayName = matcher.group(1);
                                String filterField = matcher.group(2);
                                String filterValue = matcher.group(3);
                                currentNode = currentNode.get(arrayName);
                                if (filterValue.equals("-1")) {
                                    // 遍历整个数组
                                    for (JsonNode element : currentNode) {
                                        JsonNode childNode = extractNestedField(element, Arrays.copyOfRange(pathParts, Arrays.asList(pathParts).indexOf(part) + 1, pathParts.length));
                                        if (childNode != null && childNode.isValueNode()) {
                                            finalResults.add(childNode.asText()); // 或其他适合的类型
                                        }
                                    }
                                    break;
                                } else {
                                    // 根据 filterValue 过滤数组
                                    currentNode = filterArray(currentNode, filterField, filterValue);
                                    if (currentNode == null) {
                                        log.info("array筛选条件异常，结果为空，条件={}", filterField + "=" + filterValue);
                                        break;
                                    }
                                }
                            }
                        } else {
                            currentNode = currentNode.get(part);
                        }
                    }
                    if (currentNode != null && !currentNode.isArray()) {
                        Object finalValue = isFloatNumber(currentNode.asText()) ? Double.valueOf(currentNode.asText()) : currentNode.asText();
                        map.put(newPath, finalValue);
                    } else if (!finalResults.isEmpty()) {
                        map.put(newPath, finalResults);
                    }
                }
            }
            iMqttSender.sendToMqtt("guoqi/web/custom/" + uuid + "/INFO", JSON.toJSONString(map));
            log.info("解析耗时:{}", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("解析数据调用异常:", e);
        }
    }

    @Override
    public void parseCustomSignal(String topic, String msg) {
        String uuid = StringUtils.substringAfterLast(topic, "/");
        if (!redisUtil.hasKey(uuid + "parseCustomSignal")) {
            log.info("自定义信号:{},表达式池为空");
            return;
        }
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> info = new HashMap<>();
            Map<String, Object> map = mapper.readValue(msg, new TypeReference<Map<String, Object>>() {
            });
            if (map.size() < 2) {
                return;
            }
            List<CustomSignalParesRequest> customSignalParesRequests = redisUtil.hGetAll(uuid + "parseCustomSignal", CustomSignalParesRequest.class);
            customSignalParesRequests.forEach(i -> {
                Object response = null;
                try {
                    response = AviatorEvaluator.execute(i.getExpression(), map);
                } catch (InvalidDataException e) {
                    log.error("自定义信号表达式解析，参数无效 {}", e.getMessage());
                }
                info.put(i.getName(), response);
            });
            result.put("info", info);
            result.put("timestamp", map.get("timestamp"));
            iMqttSender.sendToMqtt("guoqi/web/target/custom/result/" + uuid, JSON.toJSONString(result));
        } catch (IOException e) {
            log.error("自定义信号解析异常", e);
        }


    }

    private JsonNode extractNestedField(JsonNode node, String[] remainingPathParts) {
        JsonNode currentNode = node;
        for (String part : remainingPathParts) {
            currentNode = currentNode.get(part);
            if (currentNode == null) {
                break;
            }
        }
        return currentNode;
    }

    private JsonNode filterArray(JsonNode arrayNode, String filterField, String filterValue) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }
        for (JsonNode element : arrayNode) {
            JsonNode fieldNode = element.get(filterField);
            if (fieldNode != null && filterValue.equals(fieldNode.asText())) {
                return element;
            }
        }
        return null;
    }


    private boolean isFloatNumber(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private JsonNode findDataNode(JsonNode rootNode, String esn, String name) {
        for (JsonNode node : rootNode) {
            if (node.get("esn").asText().equals(esn) && node.get("table").asText().equals(name)) {
                String dataJson = node.get("data").asText();
                try {
                    return mapper.readTree(dataJson);
                } catch (Exception e) {
                    log.info("数据解析-findDataNode:", e);
                    return null;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String recordId = "recordId=65f3fda5b218482f1f141353";
        recordId = recordId.replaceAll("^\"|\"$", "");
        System.out.println(recordId);
    }
}

