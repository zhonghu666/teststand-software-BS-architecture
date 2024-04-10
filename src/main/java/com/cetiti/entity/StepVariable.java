package com.cetiti.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.cetiti.constant.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import utils.entity.BusinessException;
import utils.entity.InvalidDataException;

import java.io.Serializable;
import java.util.*;


@Data
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class StepVariable implements Serializable {

    private Map<String, ValueWrapper<?>> attributes = new LinkedHashMap<>();

    public <T> void addAttribute(String key, T value, String desc, Info info) {
        attributes.put(key, ValueWrapper.of(value, desc, info));
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        ValueWrapper<T> wrapper = (ValueWrapper<T>) attributes.get(key.trim());
        return (wrapper != null) ? wrapper.getValue() : null;
    }

    public ValueType getTypeByPath(String path) {
        Object value = getValueByPath(path);
        if (value instanceof Number) {
            return ValueType.NUMBER;
        } else if (value instanceof String) {
            return ValueType.STRING;
        } else if (value instanceof Boolean) {
            return ValueType.BOOLEAN;
        } else if (value instanceof List) {
            return ValueType.LIST;
        } else if (value instanceof StepVariable) {
            return ValueType.TREE_NODE;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueByPath(String path) {
        String[] keys = path.split("\\.");
        StepVariable current = this;
        Object value = null;
        List<?> list = null;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (current == null) {
                return null;
            }
            // 检查当前键是否为索引
            if (key.matches("\\[\\d+]")) {
                int index = Integer.parseInt(key.replaceAll("[\\[\\]]", ""));
                if (value instanceof List) {
                    list = (List<?>) value;
                    if (index >= 0 && index < list.size()) {
                        value = list.get(index);
                    } else {
                        throw new BusinessException("Index out of bounds for list: " + key + "list size:" + list.size());
                    }
                } else {
                    throw new InvalidDataException("510", "Attempting to index a non-list value: " + key);
                }
            } else {
                value = current.getAttribute(key);
            }
            if (value instanceof StepVariable) {
                current = (StepVariable) value;
            } else if (value instanceof List) {
                list = (List<?>) value;
                if (i < keys.length - 1 && !(keys[i + 1].matches("\\[\\d+]"))) {
                    String remainingPath = String.join(".", Arrays.copyOfRange(keys, i + 1, keys.length));
                    List<Object> results = new ArrayList<>();
                    for (Object obj : list) {
                        results.add(((StepVariable) obj).getValueByPath(remainingPath));
                    }
                    return (T) results;
                }
            }
        }

        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.error("路径获取变量异常,路径:{}", path, e);
            throw new BusinessException("The type of the value does not match the expected type.");
        }
    }


    public static class ValueWrapper<T> implements Serializable {
        private ValueType type;
        private T value;
        private String desc;

        private Info info;

        @JsonCreator
        public ValueWrapper(@JsonProperty("type") ValueType type,
                            @JsonProperty("value") T value,
                            @JsonProperty("desc") String desc,
                            @JsonProperty("info") Info info) {
            this.type = type;
            this.value = value;
            this.desc = desc;
            this.info = info;
        }

        public static <T> ValueWrapper<T> of(T value, String desc, Info info) {
            if (value instanceof String) {
                return new ValueWrapper<>(ValueType.STRING, value, desc, info);
            } else if (value instanceof Number) {
                return new ValueWrapper<>(ValueType.NUMBER, value, desc, info);
            } else if (value instanceof Boolean) {
                return new ValueWrapper<>(ValueType.BOOLEAN, value, desc, info);
            } else if (value instanceof List) {
                return new ValueWrapper<>(ValueType.LIST, value, desc, info);
            } else if (value instanceof StepVariable) {
                return new ValueWrapper<>(ValueType.TREE_NODE, value, desc, info);
            } else {
                throw new IllegalArgumentException("Unsupported type");
            }
        }

        public ValueType getType() {
            return type;
        }

        public T getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Info getInfo() {
            return info;
        }
    }

    public void addNestedAttributeObject(String nestedKey, Object value, String desc) {
        if (value instanceof Number) {
            addNestedAttribute(nestedKey, (Number) value, desc);
        } else if (value instanceof String) {
            addNestedAttribute(nestedKey, (String) value, desc);
        } else if (value instanceof Boolean) {
            addNestedAttribute(nestedKey, (Boolean) value, desc);
        } else if (value instanceof List) {
            // 处理列表类型
            addNestedAttribute(nestedKey, (List<?>) value, desc);
        } else if (value instanceof StepVariable) {
            // 处理 StepVariable 类型
            addNestedAttribute(nestedKey, (StepVariable) value, desc);
        } else {
            // 处理其他可能的类型或抛出异常
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
    }


    public <T> void addNestedAttribute(String nestedKey, T value, String desc) {
        String[] keys = nestedKey.split("\\.");
        Map<String, ValueWrapper<?>> currentMap = attributes;
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            // 检查是否为列表索引
            boolean isListIndex = i < keys.length - 1 && keys[i + 1].matches("\\[\\d+]");
            ValueWrapper<?> wrapper = currentMap.computeIfAbsent(key, k -> new ValueWrapper<>(ValueType.TREE_NODE, new StepVariable(), "Nested StepVariable", null));
            if (isListIndex) {
                // 如果是列表索引，则解析索引并进行处理
                int index = Integer.parseInt(keys[i + 1].replaceAll("[\\[\\]]", ""));
                List<?> list = (List<?>) wrapper.getValue();
                if (list == null || index >= list.size()) {
                    throw new BusinessException("Index out of bounds for list: " + key);
                }
                if (i == keys.length - 2) {
                    // 如果是倒数第二个键，替换列表中的元素
                    ((List) list).set(index, value);
                    return;
                } else {
                    // 否则继续深入到列表元素的属性中
                    wrapper = (ValueWrapper<?>) list.get(index);
                    currentMap = ((StepVariable) wrapper.getValue()).attributes;
                    i++;  // 跳过下一个键，因为已经处理了索引
                }
            } else {
                // 如果不是列表索引，则继续深入到下一层级
                currentMap = ((StepVariable) wrapper.getValue()).attributes;
            }
        }
        String finalKey = keys[keys.length - 1];
        ValueWrapper finalWrapper = currentMap.get(finalKey);
        if (finalWrapper != null) {
            ValueType valueWrapperType = getValueWrapperType(value);
            if (finalWrapper.getType().equals(valueWrapperType)) {
                finalWrapper.setValue(value);
            } else {
                currentMap.put(finalKey, ValueWrapper.of(value, desc, null));
            }
        } else {
            currentMap.put(finalKey, ValueWrapper.of(value, desc, null));
        }
    }

    private <T> ValueType getValueWrapperType(T value) {
        if (value instanceof Number) {
            return ValueType.NUMBER;
        } else if (value instanceof String) {
            return ValueType.STRING;
        } else if (value instanceof Boolean) {
            return ValueType.BOOLEAN;
        } else if (value instanceof List) {
            return ValueType.LIST;
        } else if (value instanceof StepVariable) {
            return ValueType.TREE_NODE;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
    }


    public static StepVariable RESULT_SUCCESS(StepStatus status) {
        StepVariable step = new StepVariable();
        step.addNestedAttribute("Result.Error.Code", ErrorCode.SUCCESS.getCode(), "code");
        step.addNestedAttribute("Result.Error.Msg", ErrorCode.SUCCESS.getDesc(), "Msg");
        step.addNestedAttribute("Result.Error.Occurred", ErrorCode.SUCCESS.getOccurred(), "Occurred");
        step.addNestedAttribute("Result.Status", status.getCode(), "Status");
        step.addNestedAttribute("Result.ReportTest", "", "ReportTest");
        step.addNestedAttribute("Result.Common", new StepVariable(), "Common");
        step.addNestedAttribute("Result.DataSource", "", "DataSource");
        return step;
    }

    public static StepVariable RESULT_Fail(StepStatus status) {
        StepVariable step = new StepVariable();
        step.addNestedAttribute("Result.Error.Code", ErrorCode.COMMON_FAIL.getCode(), "code");
        step.addNestedAttribute("Result.Error.Msg", ErrorCode.COMMON_FAIL.getDesc(), "Msg");
        step.addNestedAttribute("Result.Error.Occurred", ErrorCode.COMMON_FAIL.getOccurred(), "Occurred");
        step.addNestedAttribute("Result.Status", status.getCode(), "Status");
        step.addNestedAttribute("Result.ReportTest", "", "ReportTest");
        step.addNestedAttribute("Result.Common", new StepVariable(), "Common");
        step.addNestedAttribute("Result.DataSource", "", "DataSource");
        return step;
    }


    public static StepVariable RESULT(String stepType, TestStepType subType, StepStatus status, ErrorCode errorCode) {
        StepVariable step = new StepVariable();
        step.addNestedAttribute("Result.Error.Code", errorCode.getCode(), "code");
        step.addNestedAttribute("Result.Error.Msg", errorCode.getDesc(), "Msg");
        step.addNestedAttribute("Result.Error.Occurred", errorCode.getOccurred(), "Occurred");
        step.addNestedAttribute("Result.Status", "", "Status");
        step.addNestedAttribute("Result.ReportTest", "", "ReportTest");
        step.addNestedAttribute("Result.Common", new StepVariable(), "Common");
        step.addNestedAttribute("Result.DataSource", "", "DataSource");
        switch (stepType) {
            case "N_LABEL":
                step.addNestedAttribute("desc", "模版", "标签描述");
                break;
            case "N_WAIT":
                step.addNestedAttribute("TimeoutExpr", 0, "等待时间");
                break;
            case "N_MESSAGE_POPUP":
                step.addNestedAttribute("MessageExpr", "message", "内容");
                step.addNestedAttribute("TitleExpr", "title", "标题");
                step.addNestedAttribute("ButtonLabel1", "按键1", "按键内容");
                step.addNestedAttribute("ButtonLabel2", "按键2", "按键内容");
                step.addNestedAttribute("ButtonLabel3", "按键3", "按键内容");
                step.addNestedAttribute("ButtonLabel4", "按键4", "按键内容");
                step.addNestedAttribute("ButtonLabel5", "按键5", "按键内容");
                step.addNestedAttribute("ButtonLabel6", "按键6", "按键内容");
                StepVariable msg = new StepVariable();
                msg.addNestedAttribute("Information1", "信息展示1", "信息展示");
                step.addToListAtPath("Information", msg);
                StepVariable reply = new StepVariable();
                reply.addNestedAttribute("replyText1", "应答文本1", "应答文本");
                step.addToListAtPath("replyText", reply);
                step.addNestedAttribute("Result.ButtonHit", 1, "焦点按钮id");
                step.addNestedAttribute("TimeOutFlag", true, "是否超时设置");
                step.addNestedAttribute("TimeOut", 1, "超时时间");
                break;
            case "N_SEQUENCE_CALL":
                step.addNestedAttribute("childTestSequenceId", "子序列Id", "子序列Id");
                break;
            case "N_FLOW_CONTROL":
                step.addNestedAttribute("FlowStatus", false, "流控表达式结果");
                step.addNestedAttribute("Expression", "condition", "表达式");
                step.addNestedAttribute("FlowControlType", "subType", "流控类型");
                step.addNestedAttribute("endType", "END_UNDEFINED", "end类型");
                break;
            case "N_STATEMENT":
                step.addNestedAttribute("Expression", "expression", "表达式");
                break;
            case "N_ACTION":
            case "N_TEST":
                switch (subType) {
                    case T_STRING_VALUE:
                        step.addNestedAttribute("Limits.StringExpr", "条件", "字符串");
                        step.addNestedAttribute("String", "参数", "参数");
                        break;
                    case T_MULTIPLE_NUMERIC_LIMIT:
                        StepVariable Measurement = new StepVariable();
                        Measurement.addNestedAttribute("Limits.HighExpr", "high", "上限");
                        Measurement.addNestedAttribute("Limits.LowExpr", "low", "下限");
                        Measurement.addNestedAttribute("Limits.Expression", "Expression", "表达式");
                        Measurement.addNestedAttribute("Status", "status", "结果");
                        Measurement.addNestedAttribute("Units", "unit", "单位");
                        step.addToListAtPath("Measurement", Measurement);
                        break;
                    case T_NUMERIC_LIMIT:
                        step.addNestedAttribute("Limits.HighExpr", "high", "上限");
                        step.addNestedAttribute("Limits.LowExpr", "low", "下限");
                        step.addNestedAttribute("Limits.Expression", "Expression", "表达式");
                        step.addNestedAttribute("Units", "unit", "单位");
                        break;
                    case T_PASS_FAIL:
                        step.addNestedAttribute("Result.PassFail", true, "是否通过");
                        break;
                }
                break;
        }
        return step;
    }

    public void addToListAtPathObject(String path, Object value) {
        if (value instanceof Number) {
            addToListAtPath(path, (Number) value);
        } else if (value instanceof String) {
            addToListAtPath(path, (String) value);
        } else if (value instanceof Boolean) {
            addToListAtPath(path, (Boolean) value);
        } else if (value instanceof List) {
            // 处理列表类型
            addToListAtPath(path, (List<?>) value);
        } else if (value instanceof StepVariable) {
            // 处理 StepVariable 类型
            addToListAtPath(path, (StepVariable) value);
        } else {
            // 处理其他可能的类型或抛出异常
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
    }


    public <T> void addToListAtPath(String path, T value) {
        String[] keys = path.split("\\.");
        List<StepVariable> currentList = new ArrayList<>();
        currentList.add(this);

        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            List<StepVariable> nextLevelList = new ArrayList<>();
            for (StepVariable current : currentList) {
                ValueWrapper<?> wrapper = current.attributes.get(key);
                if (wrapper != null) {
                    if (wrapper.getValue() instanceof StepVariable) {
                        if (wrapper.getValue() != null) {
                            nextLevelList.add((StepVariable) wrapper.getValue());
                        }
                    } else if (wrapper.getValue() instanceof List) {
                        for (Object item : (List<?>) wrapper.getValue()) {
                            if (item instanceof StepVariable) {
                                nextLevelList.add((StepVariable) item);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Path element is not a StepVariable or List: " + key);
                    }
                } else {
                    // 创建一个新的 StepVariable 并加入到路径中
                    StepVariable newVariable = new StepVariable();
                    current.attributes.put(key, ValueWrapper.of(newVariable, "", null));
                    nextLevelList.add(newVariable);
                }
            }
            currentList = nextLevelList;
        }

        String finalKey = keys[keys.length - 1];
        for (StepVariable current : currentList) {
            ValueWrapper<?> finalWrapper = current.attributes.get(finalKey);
            if (finalWrapper == null || !(finalWrapper.getValue() instanceof List)) {
                // 如果最终的键不存在或者不是 List，则创建一个新的 List 并添加元素
                List<T> newList = new ArrayList<>();
                newList.add(value);
                current.attributes.put(finalKey, ValueWrapper.of(newList, "", null));
            } else {
                // 如果最终的键已存在且是 List，则直接添加元素
                List<T> value1 = (List<T>) finalWrapper.getValue();
                ((List<T>) finalWrapper.getValue()).add(value);
            }
        }
    }
    public boolean removeAttributeByPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String[] keys = path.split("\\.");
        Map<String, ValueWrapper<?>> currentMap = attributes;
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            ValueWrapper<?> wrapper = currentMap.get(key);
            if (wrapper == null || !(wrapper.getValue() instanceof StepVariable)) {
                return false; // Path is invalid or reaches a non-existent variable
            }
            StepVariable stepVar = (StepVariable) wrapper.getValue();
            currentMap = stepVar.attributes;
        }

        // Remove the final key
        String finalKey = keys[keys.length - 1];
        if (currentMap.containsKey(finalKey)) {
            currentMap.remove(finalKey);
            return true;
        }
        return false;
    }
}
