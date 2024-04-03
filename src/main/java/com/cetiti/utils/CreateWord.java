package com.cetiti.utils;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.FlowControlType;
import com.cetiti.constant.SpinType;
import com.cetiti.constant.StepStatus;
import com.cetiti.constant.TestStepType;
import com.cetiti.entity.StepAdditional;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.step.ActionStep;
import com.cetiti.entity.step.StepBase;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cetiti.constant.ActionType.SCENE;
import static com.cetiti.constant.SpinType.CANNED_CYCLE;

@Slf4j
public class CreateWord {

    public static void main(String[] args) {
        StepVariable stepVariable = new StepVariable();
        stepVariable.addNestedAttribute("Local.Data.a", Arrays.asList(1, 5, 6), "1d");

        List<StepVariable> list = new ArrayList<>();
        StepVariable stepVariable1 = new StepVariable();
        stepVariable1.addNestedAttribute("id", 1, "id");
        stepVariable1.addNestedAttribute("uid", 6, "uid");
        stepVariable1.addNestedAttribute("xid", 8, "xid");

        StepVariable stepVariable2 = new StepVariable();
        stepVariable2.addNestedAttribute("id", 2, "id");
        stepVariable2.addNestedAttribute("uid", 9, "uid");
        stepVariable2.addNestedAttribute("xid", 18, "xid");

        StepVariable stepVariable3 = new StepVariable();
        stepVariable3.addNestedAttribute("id", 7, "id");
        stepVariable3.addNestedAttribute("uid", 22, "uid");
        stepVariable3.addNestedAttribute("xid", 30, "xid");

        list.add(stepVariable1);
        list.add(stepVariable2);
        list.add(stepVariable3);
        stepVariable.addNestedAttribute("Local.Data.b", list, "2d");

        List<?> valueByPath = stepVariable.getValueByPath("Local.Data.a");
        Object o = valueByPath.get(0);
        System.out.println(o instanceof Number);

        List<?> valueByPath1 = stepVariable.getValueByPath("Local.Data.b");
        Object o1 = valueByPath1.get(0);
        System.out.println(o1 instanceof StepVariable);

        System.out.println(JSON.toJSONString(stepVariable));

        List<StepAdditional> list1 = new ArrayList<>();
        StepAdditional stepAdditional = new StepAdditional();
        stepAdditional.setSourceExpression("Local.Data.a");
        stepAdditional.setIsGraphs(true);

        StepAdditional stepAdditional2 = new StepAdditional();
        stepAdditional2.setSourceExpression("Local.Data.b");
        stepAdditional2.setIsGraphs(true);
        list1.add(stepAdditional);
        list1.add(stepAdditional2);

        List<byte[]> chart = ChartUtil.chart(list1, stepVariable);
        System.out.println(chart.size());

        int i = 0;
        for (byte[] bytes : chart) {
            // 将byte数组保存到本地文件

            try (FileOutputStream outputStream = new FileOutputStream(i + ".png")) {
                outputStream.write(bytes);
                System.out.println("图表保存成功！");
            } catch (IOException e) {
                System.out.println("图表保存失败：" + e.getMessage());
            }
            i++;
        }
    }


    public static void loop(XWPFTable table, Boolean loopResult, StepVariable stepVariable, StepBase stepBase) {
        String bashPath = "RunState.SequenceFile.Data.Seq." + stepBase.getTestSequenceName() + "." + stepBase.getScope() + "." + stepBase.getName() + "[" + stepBase.getId() + "]";

        //循环的结果
        Boolean b = stepVariable.getValueByPath(bashPath + ".LoopStatus");
        String result = b ? "合格" : "失败";
        Integer all = stepVariable.getValueByPath(bashPath + ".LoopNumIterations");
        Integer passed = stepVariable.getValueByPath(bashPath + ".LoopNumPassed");
        Integer failed = stepVariable.getValueByPath(bashPath + ".LoopNumFailed");
        TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepBase.getName() + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
        TableUtil.fillTableData(table, result);
        TableUtil.createRowAndFill(table, 500, Arrays.asList("循环次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(all));
        TableUtil.createRowAndFill(table, 500, Arrays.asList("成功次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(passed));
        TableUtil.createRowAndFill(table, 500, Arrays.asList("失败次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(failed));
        if (loopResult) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[1]"), 200);
            TableUtil.fillTableData(table, "完成");
            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[2]"), 200);
            TableUtil.fillTableData(table, "完成");
        }
    }


    public static void ifTable(XWPFTable table, String type, String childrenType, Boolean loopConfig, Boolean loopResult) {
        //test 步骤
        if (Objects.equals(type, "test")) {
            //Test步骤类型 testType
            if (Objects.equals(childrenType, "PASS")) {
                //todo
                TableUtil.createRowAndFill(table, 1500, Arrays.asList("合格/失败测试", "2023-9-20 16:24:30.123"));
                TableUtil.fillTableData(table, "合格", "Result.Numeric");

            } else if (Objects.equals(childrenType, "NUMBER")) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList("数值限度测试", "2023-9-20 16:24:30.126"));
                TableUtil.fillTableData(table, "合格");

            } else if (Objects.equals(childrenType, "NUMBER_MANY")) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList("多数值限度测试", "2023-9-20 16:24:30.126"));
                TableUtil.fillTableData(table, "合格");
                TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("执行结果:"), 200);
                TableUtil.createRowAndFill(table, 500, Arrays.asList("NumericArray[0]"), 400);
                TableUtil.createRowAndFill(table, 500, Arrays.asList("NumericArray[1]"), 400);
                //todo 填充数据

            } else if (Objects.equals(childrenType, "STRING")) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList("字符串测试", "2023-9-20 16:24:30.126"));
                TableUtil.fillTableData(table, "合格");
            }

        } else if (Objects.equals(type, "action")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList("Action类步骤", "2023-9-20 16:24:30.126"));
            TableUtil.fillTableData(table, "完成");
            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("ActionSettings"), 200);
//            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Error Message"), 200);

        } else if (Objects.equals(type, "popup")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList("Message Popup步骤", "2023-9-20 16:24:30.126"));
            TableUtil.fillTableData(table, "完成");
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("执行结果:"), 200);
            TableUtil.createRowAndFill(table, 500, Arrays.asList("ButtonHit"), 400);
            TableUtil.createRowAndFill(table, 500, Arrays.asList("Response"), 400);

        } else if (Objects.equals(type, "call")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList("序列调用步骤", "2023-9-20 16:24:30.126"));
            TableUtil.fillTableData(table, "合格");
        } else if (Objects.equals(type, "statement")) {
            TableUtil.createRowAndFill(table, 1500, Arrays.asList("声明步骤", "表达式内容", "2023-9-20 16:24:30.123"));
            TableUtil.fillTableData(table, "合格");
        } else if (Objects.equals(type, "for")) {
            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("For", "{0} 设置的循环语句", "2023-9-20 16:24:30.123"));
        } else if (Objects.equals(type, "end for")) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (For)"));
        } else if (Objects.equals(type, "if")) {

            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("If", "设置的条件语句", "2023-9-20 16:24:30.123"));
        } else if (Objects.equals(type, "else if")) {

            TableUtil.createRowAndMergeFill(table, 1000, 0, 7, Arrays.asList("Else If", "设置的条件语句"));
        } else if (Objects.equals(type, "else")) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Else"));
        } else if (Objects.equals(type, "end if")) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (If)"));
        } else if (Objects.equals(type, "select")) {

            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Select", "设置的待比较值", "2023-9-20 16:24:30.123"));
        } else if (Objects.equals(type, "case")) {

            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Case 0"));
        } else if (Objects.equals(type, "end case")) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Case)"));
        } else if (Objects.equals(type, "end select")) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Select)"));
        } else if (Objects.equals(type, "while")) {

            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("While", "设置的条件语句", "2023-9-20 16:24:30.123"));
        } else if (Objects.equals(type, "end while")) {

            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (While)"));
        } else if (Objects.equals(type, "do while")) {

            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Do While", "设置的条件语句", "2023-9-20 16:24:30.123"));
        } else if (Objects.equals(type, "end do while")) {

            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Do While)"));
        }


        //todo 循环配置
        if (loopConfig) {
            loop(table, loopResult);
        }
    }

    public static void loop(XWPFTable table, Boolean loopResult) {
        TableUtil.createRowAndFill(table, 1000, Arrays.asList("××步骤循环", "2023-9-20 16:24:30.126"));
        TableUtil.fillTableData(table, "合格");
        TableUtil.createRowAndFill(table, 500, Arrays.asList("循环次数"), 200);
        TableUtil.fillTableData(table, "2");
        TableUtil.createRowAndFill(table, 500, Arrays.asList("成功次数"), 200);
        TableUtil.fillTableData(table, "2");
        TableUtil.createRowAndFill(table, 500, Arrays.asList("失败次数"), 200);
        TableUtil.fillTableData(table, "0");
        if (loopResult) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[1]"), 200);
            TableUtil.fillTableData(table, "完成");
            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[2]"), 200);
            TableUtil.fillTableData(table, "完成");
        }
    }


    public static List<String> word(XWPFTable table, StepVariable stepVariable, String testSequenceName) {
        //子序列id
        List<String> idList = new ArrayList<>();
        String path = "RunState.SequenceFile.Data.Seq." + testSequenceName;
        StepVariable stepVariable1 = stepVariable.getValueByPath(path);

        Map<String, StepVariable.ValueWrapper<?>> attributes = stepVariable1.getAttributes();
        //scope
        Set<String> scopeStrings = attributes.keySet();
        for (String scopeString : scopeStrings) {
            log.info("scopeName=============:{}", scopeString);
            StepVariable scopeVariable = stepVariable1.getValueByPath(scopeString);
            Map<String, StepVariable.ValueWrapper<?>> attributes1 = scopeVariable.getAttributes();
            //步骤名[id]
            Set<String> nameStrings = attributes1.keySet();
            for (String nameString : nameStrings) {
                log.error("============== 步骤名[id]:{}", nameString);
                //步骤名
                String stepName = nameString.substring(0, nameString.lastIndexOf("["));
                //步骤id
                String stepId = nameString.substring(nameString.lastIndexOf("[") + 1, nameString.lastIndexOf("]"));
                MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
                StepBase stepBase = mongoTemplate.findById(stepId, StepBase.class);
                //步骤变量
                StepVariable stepVariable2 = scopeVariable.getValueByPath(nameString);
                log.info("stepVariable2:{}", JSON.toJSONString(stepVariable2));
                //步骤类型
                String type = stepVariable2.getValueByPath("type");
                log.info("type:{}", type);
                //Label不展示
                if (Objects.equals(type, "N_LABEL")) {
                    continue;
                }
                //是否进入报告
                Integer resultRecordStatus = stepVariable2.getValueByPath("resultRecordStatus");
                log.info("resultRecordStatus:{}", resultRecordStatus);
                //步骤状态
                String status = stepVariable2.getValueByPath("Result.Status");
                log.info("status:{}", status);
                log.info("=============================================================================");
                //是否循环
                String loop = stepVariable2.getValueByPath("LoopType");
                boolean loopFlag = loop != null && !Objects.equals(loop, SpinType.NONE.name());
                log.info("通用属性循环:{}=====", loopFlag);

                //序列调用
                if (Objects.equals(type, "N_SEQUENCE_CALL")) {
                    String childTestSequenceId = stepVariable2.getValueByPath("childTestSequenceId");
                    idList.add(childTestSequenceId);
                }
                //记录到报告
                if (resultRecordStatus == 0) {
                    if (loopFlag) {
                        forLoop(table, stepVariable2, stepName, stepBase);
                        addExtraResults(table, stepBase.getStepAdditionalList(), stepVariable);
                    } else {
                        if (Objects.equals(type, "N_TEST")) {
                            String subType = stepVariable2.getValueByPath("TestStepType");
                            if (Objects.equals(subType, TestStepType.T_PASS_FAIL.name())) {
                                Boolean result = stepVariable2.getValueByPath("Result.PassFail");
                                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                                TableUtil.fillTableData(table, StepStatus.getDescByCode(status), String.valueOf(result));
                            } else if (Objects.equals(subType, TestStepType.T_NUMERIC_LIMIT.name())) {
                                Object numeric = stepVariable2.getValueByPath("Result.Numeric");
                                String units = stepVariable2.getValueByPath("Result.Units");
                                String low = stepVariable2.getValueByPath("Limits.LowExpr");
                                String high = stepVariable2.getValueByPath("Limits.HighExpr");
                                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                                TableUtil.fillTableData(table, StepStatus.getDescByCode(status), numeric == null ? "" : String.valueOf(numeric), units, "", low, high);
                            } else if (Objects.equals(subType, TestStepType.T_MULTIPLE_NUMERIC_LIMIT.name())) {
                                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                                TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                                TableUtil.createRowAndMergeFill(table, 500, 0, 7, List.of("执行结果:"), 200);
                                List<StepVariable> list = stepVariable2.getValueByPath("Measurement");
                                for (int i = 0; i < list.size(); i++) {
                                    String strArr = "NumericArray[" + i + "]";
                                    TableUtil.createRowAndFill(table, 500, List.of(strArr), 400);
                                    Object numeric = list.get(i).getValueByPath("Result.Numeric");
                                    String units = list.get(i).getValueByPath("Units");
                                    String low = list.get(i).getValueByPath("Limits.LowExpr");
                                    String high = list.get(i).getValueByPath("Limits.HighExpr");
                                    TableUtil.fillTableData(table, "", numeric == null ? "" : String.valueOf(numeric), units, "", low, high);
                                }
                            } else if (Objects.equals(subType, TestStepType.T_STRING_VALUE.name())) {
                                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                                String low = stepVariable2.getValueByPath("Limits.StringExpr");
                                String str = stepVariable2.getValueByPath("String");
                                TableUtil.fillTableData(table, StepStatus.getDescByCode(status), str, "", "", low);
                            }

                        } else if (Objects.equals(type, "N_ACTION")) {
                            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                            TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                            ActionStep actionStep = (ActionStep) stepBase;
                            //记录动作配置
                            Boolean enable = stepVariable2.getValueByPath("ActionSettings.configEnable");
                            if (enable) {
                                TableUtil.createRowAndMergeFill(table, 1000, 1, 7, List.of("ActionSettings"), 200);
                                switch (actionStep.getActionType()) {
                                    case CONFIGURATION:
                                        switch (actionStep.getDeviceType()) {
                                            case RSU:
                                                StringBuilder stringBuilder = getStringBuilder(actionStep);
                                                TableUtil.fillTableData(table, stringBuilder.toString());
                                                break;
                                            case MEC:
                                                TableUtil.fillTableData(table, "设备配置-" + actionStep.getDeviceType() + "/" + actionStep.getEsn() + "-MEC交通事件配置");
                                                break;
                                            case SIGNAL:
                                                TableUtil.fillTableData(table, "设备配置-信号机/" + actionStep.getEsn() + "-信号机控制");
                                                break;
                                            case OBU:
                                                TableUtil.fillTableData(table, "设备配置-" + actionStep.getDeviceType() + "/" + actionStep.getEsn() + "-OBU场景算法配置");
                                                break;
                                            case DATA_RECORDING_DEVICE:
                                                switch (actionStep.getAction()) {
                                                    case 1:
                                                        TableUtil.fillTableData(table, "设备配置-智能网联数据记录终端/" + actionStep.getEsn() + "-开始存");
                                                        break;
                                                    case 2:
                                                        TableUtil.fillTableData(table, "设备配置-智能网联数据记录终端/" + actionStep.getEsn() + "-结束存");
                                                        break;
                                                    case 3:
                                                        TableUtil.fillTableData(table, "设备配置-智能网联数据记录终端/" + actionStep.getEsn() + "-开始传");
                                                        break;
                                                    case 4:
                                                        TableUtil.fillTableData(table, "设备配置-智能网联数据记录终端/" + actionStep.getEsn() + "-结束传");
                                                        break;
                                                }
                                                break;
                                        }
                                        break;
                                    case SCENE:
                                        String deviceNames = actionStep.getSceneDistributeConfigDto().getRsuSceneConfigs().stream().map(i -> i.getDeviceName()).collect(Collectors.joining(","));
                                        String sceneInfo = "场景下发-" + (actionStep.getChildrenType() == 1 ? "数据预置-" : "数据广播-") + actionStep.getSceneName() + "-" + deviceNames;
                                        TableUtil.fillTableData(table, sceneInfo);
                                        break;
                                }
                                if (Objects.equals(status, StepStatus.ERROR.getCode())) {
                                    TableUtil.createRowAndMergeFill(table, 1000, 1, 7, List.of("Error Message"), 200);
                                    String code = stepVariable2.getValueByPath("Result.Error.Code");
                                    TableUtil.fillTableData(table, stepName + "执行错误, Error Code: " + code);
                                }
                            }
                        } else if (Objects.equals(type, "N_MESSAGE_POPUP")) {
                            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                            TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                            TableUtil.createRowAndMergeFill(table, 500, 0, 7, List.of("执行结果:"), 200);
                            TableUtil.createRowAndMergeFill(table, 500, 1, 7, List.of("ButtonHit"), 400);
                            String chooseButton = stepVariable2.getValueByPath("ChooseButton");
                            TableUtil.fillTableData(table, chooseButton == null ? "" : chooseButton);

                            TableUtil.createRowAndMergeFill(table, 500, 1, 7, List.of("Response"), 400);
                            StepVariable responseStep = stepVariable2.getValueByPath("replyText");
                            if (responseStep != null) {
                                Map<String, StepVariable.ValueWrapper<?>> responseMap = responseStep.getAttributes();
                                List<Object> list = responseMap.values().stream().map(StepVariable.ValueWrapper::getValue).collect(Collectors.toList());
                                TableUtil.fillTableData(table, list.toString());
                            }
                        } else if (Objects.equals(type, "N_SEQUENCE_CALL")) {
                            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                            TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                        } else if (Objects.equals(type, "N_STATEMENT")) {
                            String expression = stepVariable2.getValueByPath("Expression");
                            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName, DateUtils.localDate2LongString(LocalDateTime.now())));
                            TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Collections.singletonList(expression));
                        } else if (Objects.equals(type, "N_FLOW_CONTROL")) {
                            //子类型
                            String subType = stepVariable2.getValueByPath("FlowControlType");
                            Boolean flowStatus = stepVariable2.getValueByPath("FlowStatus");
                            String condition = stepVariable2.getValueByPath("Expression");
                            if (Objects.equals(subType, FlowControlType.F_FOR.name())) {
                                TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("For", condition, DateUtils.localDate2LongString(LocalDateTime.now())));
                            } else if (Objects.equals(subType, FlowControlType.F_END.name())) {
                                //todo,流控步骤的类型，添加End（xxx）
                                TableUtil.createRowAndMergeFill(table, 500, 0, 7, List.of("End"));
                            } else if (Objects.equals(subType, FlowControlType.F_IF.name())) {
                                TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("If", condition, DateUtils.localDate2LongString(LocalDateTime.now())));
                            } else if (Objects.equals(subType, FlowControlType.F_ELSE_IF.name())) {
                                TableUtil.createRowAndMergeFill(table, 1000, 0, 7, Arrays.asList("Else If", condition));
                            } else if (Objects.equals(subType, FlowControlType.F_ELSE.name())) {
                                if (flowStatus != null && flowStatus) {
                                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, List.of("Else"));
                                }
                            } else if (Objects.equals(subType, FlowControlType.F_SELECT.name())) {
                                TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Select", condition, DateUtils.localDate2LongString(LocalDateTime.now())));
                            } else if (Objects.equals(subType, FlowControlType.F_CASE.name())) {
                                if (flowStatus != null && flowStatus) {
                                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, List.of("Case: " + condition));
                                }
                            } else if (Objects.equals(subType, FlowControlType.F_WHILE.name())) {
                                TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("While", condition, DateUtils.localDate2LongString(LocalDateTime.now())));
                            } else if (Objects.equals(subType, FlowControlType.F_DO_WHILE.name())) {
                                TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Do While", condition, DateUtils.localDate2LongString(LocalDateTime.now())));
                            }
                        }

                        //todo 额外结果
                        if (Objects.nonNull(stepBase) && !CollectionUtils.isEmpty(stepBase.getStepAdditionalList())) {
                            StringBuilder sb = new StringBuilder();
                            for (StepAdditional i : stepBase.getStepAdditionalList()) {
                                if (!i.getIsGraphs()) {
                                    Object valueByPath = stepVariable2.getValueByPath("ExtraResults." + i.getId());
                                    sb.append(valueByPath.toString());
                                    sb.append("\n");
                                }
                            }
                            //额外结果
                            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Report Text"), 200);
                            //Addition Result
                            TableUtil.createRowAndMergeFill(table, 500, 1, 7, Arrays.asList("Addition Result"), 200);
                            TableUtil.fillTableData(table, sb.toString());
                        }
                        //图表
                        if (Objects.nonNull(stepBase)) {
                            List<StepAdditional> stepAdditionalList = stepBase.getStepAdditionalList();
                            if (!CollectionUtils.isEmpty(stepAdditionalList)) {
                                stepAdditionalList = stepAdditionalList.stream().filter(stepAdditional -> Objects.equals(stepAdditional.getIsGraphs(), Boolean.TRUE)).collect(Collectors.toList());
                                List<byte[]> chart = ChartUtil.chart(stepAdditionalList, stepVariable);
                                for (int i = 0; i < stepAdditionalList.size(); i++) {
                                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, Collections.singletonList(stepAdditionalList.get(i).getTargetExpression()));
                                    TableUtil.createRowAndMergeCell(table, 3500, 0, 7);
                                    TableUtil.addPicture(table, chart.get(i), "picture");
                                }
                            }
                        }
                    }
                }

            }
        }
        return idList;
    }

    @NotNull
    private static StringBuilder getStringBuilder(ActionStep actionStep) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("设备配置-RSU/");
        stringBuilder.append(actionStep.getDeviceName());
        stringBuilder.append("-");
        if (actionStep.getRsuBroadcastDto() != null && actionStep.getRsuBroadcastDto().isChange()) {
            stringBuilder.append("RSU广播配置,");
        }
        if (actionStep.getRsuCfgDto() != null && actionStep.getRsuCfgDto().isChange()) {
            stringBuilder.append("CGF下发,");
        }
        if (actionStep.getRsuSceneConfigDto() != null && actionStep.getRsuSceneConfigDto().isChange()) {
            stringBuilder.append("场景算法使能,");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder;
    }

    public static void forLoop(XWPFTable table, StepVariable stepVariable, String stepName, StepBase stepBase) {
        //步骤类型
        String type = stepVariable.getValueByPath("type");
        //循环次数
        Double all = stepVariable.getValueByPath("LoopNumIterations");
        Integer passed = stepVariable.getValueByPath("LoopNumPassed");
        Integer failed = stepVariable.getValueByPath("LoopNumFailed");
        //每个迭代结果
        List<StepVariable> stepVariableList = stepVariable.getValueByPath("ForLoop");
        //循环结果
        Boolean b = stepVariable.getValueByPath("LoopStatus");
        String loopResult = stepBase.getCircularConfig().getSpinType().equals(CANNED_CYCLE) == b ? StepStatus.FAILED.getDesc() : StepStatus.PASSED.getDesc();
        //步骤执行结果备份
        StepVariable stepCopy = stepVariable.getValueByPath("stepCopy");

        if (Objects.equals(type, "N_TEST")) {
            String subType = stepCopy.getValueByPath("TestStepType");
            if (Objects.equals(subType, TestStepType.T_PASS_FAIL.name())) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
                TableUtil.fillTableData(table, loopResult);
                //add
                addLoopResult(table, all, passed, failed);
                if (!CollectionUtils.isEmpty(stepVariableList)) {
                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
                    for (int i = 0; i < stepVariableList.size(); i++) {
                        String status = stepVariableList.get(i).getValueByPath("Result.Status");
                        Boolean passFail = stepVariableList.get(i).getValueByPath("Result.PassFail");
                        TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "步骤[" + (i + 1) + "]"), 200);
                        TableUtil.fillTableData(table, StepStatus.getDescByCode(status), String.valueOf(passFail));
                    }
                }
            } else if (Objects.equals(subType, TestStepType.T_NUMERIC_LIMIT.name())) {
                String units = stepCopy.getValueByPath("Result.Units");
                String low = stepCopy.getValueByPath("Limits.LowExpr");
                String high = stepCopy.getValueByPath("Limits.HighExpr");

                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
                TableUtil.fillTableData(table, loopResult, "", units, "", low, high);
                //add
                addLoopResult(table, all, passed, failed);
                if (!CollectionUtils.isEmpty(stepVariableList)) {
                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
                    for (int i = 0; i < stepVariableList.size(); i++) {
                        String status = stepVariableList.get(i).getValueByPath("Result.Status");
                        Object numeric = stepVariableList.get(i).getValueByPath("Result.Numeric");
                        TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "步骤[" + (i + 1) + "]"), 200);
                        TableUtil.fillTableData(table, StepStatus.getDescByCode(status), numeric == null ? "" : String.valueOf(numeric), units, "", low, high);
                    }
                }
            } else if (Objects.equals(subType, TestStepType.T_MULTIPLE_NUMERIC_LIMIT.name())) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
                TableUtil.fillTableData(table, loopResult);
                //add
                addLoopResult(table, all, passed, failed);
                if (!CollectionUtils.isEmpty(stepVariableList)) {
                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
                    for (int i = 0; i < stepVariableList.size(); i++) {
                        String status = stepVariableList.get(i).getValueByPath("Result.Status");
                        List<StepVariable> list = stepVariableList.get(i).getValueByPath("Measurement");
                        TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "[" + i + "]:"));
                        TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                        for (int j = 0; j < list.size(); j++) {
                            String strArr = "NumericArray[" + j + "]";
                            TableUtil.createRowAndFill(table, 500, Arrays.asList(strArr), 400);
                            Object numeric = list.get(j).getValueByPath("Result.Numeric");
                            String units = list.get(j).getValueByPath("Units");
                            String low = list.get(j).getValueByPath("Limits.LowExpr");
                            String high = list.get(j).getValueByPath("Limits.HighExpr");
                            TableUtil.fillTableData(table, "", numeric == null ? "" : String.valueOf(numeric), units, "", low, high);
                        }
                    }
                }
            } else if (Objects.equals(subType, TestStepType.T_STRING_VALUE.name())) {
                TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
                TableUtil.fillTableData(table, loopResult);
                //add
                addLoopResult(table, all, passed, failed);
                if (!CollectionUtils.isEmpty(stepVariableList)) {
                    TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
                    for (int i = 0; i < stepVariableList.size(); i++) {
                        String status = stepVariableList.get(i).getValueByPath("Result.Status");
                        //todo
                        String low = stepVariableList.get(i).getValueByPath("Limits.StringExpr");
                        String str = stepVariableList.get(i).getValueByPath("String");
                        TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "步骤[" + (i + 1) + "]"), 200);
                        TableUtil.fillTableData(table, StepStatus.getDescByCode(status), str, "", "", low);
                    }
                }
            }
        } else if (Objects.equals(type, "N_ACTION")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
            TableUtil.fillTableData(table, loopResult);
            Boolean enable = stepCopy.getValueByPath("ActionSettings.configEnable");
            if (enable) {
                //todo
                TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("ActionSettings"), 200);
            }
            //add
            addLoopResult(table, all, passed, failed);
            if (!CollectionUtils.isEmpty(stepVariableList)) {
                TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
                for (int i = 0; i < stepVariableList.size(); i++) {
                    String status = stepVariableList.get(i).getValueByPath("Result.Status");
                    TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "步骤[" + (i + 1) + "]"), 200);
                    TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
                    //执行错误
                    if (Objects.equals(status, StepStatus.ERROR.getCode())) {
                        TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Error Message"), 200);
                        String code = stepVariableList.get(i).getValueByPath("Result.Error.Code");
                        TableUtil.fillTableData(table, stepName + "执行错误, Error Code: " + code);
                    }
                }
            }
        } else if (Objects.equals(type, "N_MESSAGE_POPUP")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
            TableUtil.fillTableData(table, loopResult);
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("执行结果:"), 200);
            TableUtil.createRowAndMergeFill(table, 500, 1, 7, Arrays.asList("ButtonHit"), 400);
            Integer focusButtonId = stepCopy.getValueByPath("Result.ButtonHit");
            TableUtil.fillTableData(table, focusButtonId == null ? "" : String.valueOf(focusButtonId));

            TableUtil.createRowAndMergeFill(table, 500, 1, 7, Arrays.asList("Response"), 400);
            StepVariable responseStep = stepCopy.getValueByPath("replyText");
            if (responseStep != null) {
                Map<String, StepVariable.ValueWrapper<?>> responseMap = responseStep.getAttributes();
                List<Object> list = responseMap.values().stream().map(StepVariable.ValueWrapper::getValue).collect(Collectors.toList());
                TableUtil.fillTableData(table, list.toString());
            }
            addLoopResultAndEach(table, all, passed, failed, stepVariableList, stepName);
        } else if (Objects.equals(type, "N_SEQUENCE_CALL")) {
            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
            TableUtil.fillTableData(table, loopResult);
            addLoopResultAndEach(table, all, passed, failed, stepVariableList, stepName);
        } else if (Objects.equals(type, "N_STATEMENT")) {
            String expression = stepCopy.getValueByPath("Expression");
            TableUtil.createRowAndFill(table, 1000, Arrays.asList(stepName + "步骤循环", DateUtils.localDate2LongString(LocalDateTime.now())));
            TableUtil.fillTableData(table, loopResult);
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList(expression));
            addLoopResultAndEach(table, all, passed, failed, stepVariableList, stepName);
        }
    }

    private static void addLoopResultAndEach(XWPFTable table, Double all, Integer passed, Integer failed, List<StepVariable> stepVariableList, String stepName) {
        addLoopResult(table, all, passed, failed);
        if (!CollectionUtils.isEmpty(stepVariableList)) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
            for (int i = 0; i < stepVariableList.size(); i++) {
                String status = stepVariableList.get(i).getValueByPath("Result.Status");
                TableUtil.createRowAndFill(table, 500, Arrays.asList(stepName + "步骤[" + (i + 1) + "]"), 200);
                TableUtil.fillTableData(table, StepStatus.getDescByCode(status));
            }
        }
    }


    private static void addLoopResult(XWPFTable table, Double all, Integer passed, Integer failed) {
        TableUtil.createRowAndFill(table, 500, Arrays.asList("循环次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(all));
        TableUtil.createRowAndFill(table, 500, Arrays.asList("成功次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(passed));
        TableUtil.createRowAndFill(table, 500, Arrays.asList("失败次数"), 200);
        TableUtil.fillTableData(table, String.valueOf(failed));
    }

    private static void addExtraResults(XWPFTable table, List<StepAdditional> stepAdditionalList, StepVariable stepVariable) {
        if (CollectionUtils.isEmpty(stepAdditionalList)) {
            return;
        }
        List<StepAdditional> stepAdditionalList1 = stepAdditionalList.stream().filter(StepAdditional::getIsGraphs).collect(Collectors.toList());
        List<byte[]> chart = ChartUtil.chart(stepAdditionalList1, stepVariable);
        for (int i = 0; i < stepAdditionalList1.size(); i++) {
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Collections.singletonList(stepAdditionalList1.get(i).getTargetExpression()));
            TableUtil.createRowAndMergeCell(table, 3500, 0, 7);
            TableUtil.addPicture(table, chart.get(i), "picture");
        }
        StringBuilder sb = new StringBuilder();
        stepAdditionalList.stream().filter(stepAdditional -> !stepAdditional.getIsGraphs()).forEach(i -> {
            Object valueByPath = stepVariable.getValueByPath("ExtraResults." + i.getId());
            sb.append(valueByPath.toString());
            sb.append("\n");
        });
        //额外结果
        TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Report Text"), 200);
        //Addition Result
        TableUtil.createRowAndMergeFill(table, 500, 1, 7, Arrays.asList("Addition Result"), 200);
        TableUtil.fillTableData(table, sb.toString());
    }
}
