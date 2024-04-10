package com.cetiti.service;

import com.cetiti.constant.BaseJson;
import com.cetiti.constant.TestStepType;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestSequenceConfig;
import com.cetiti.request.ExecuteRequest;
import com.cetiti.request.StartSequenceRequest;
import com.cetiti.request.TestSequenceSaveRequest;
import com.cetiti.request.batchExecuteRequest;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.response.InformationResponse;
import com.cetiti.response.StepExecuteResponse;
import com.cetiti.response.TestSequenceResponse;

import java.util.List;

public interface TestSequenceService {

    /**
     * 保存序列
     *
     * @param request
     * @return
     */
    String saveTestSequence(TestSequenceSaveRequest request);

    /**
     * 修改名称
     *
     * @param id
     * @param name
     * @return
     */
    String editTestSequenceName(String id, String name);

    /**
     * 查询所有序列
     *
     * @return
     */
    List<TestSequenceResponse> getTestSequenceAll(String token);

    /**
     * 根据ID查询序列
     *
     * @param id
     * @return
     */
    TestSequenceResponse getTestSequenceById(String id);

    /**
     * 删除序列
     *
     * @param id
     * @return
     */
    Boolean removeTestSequence(String id);


    /**
     * 执行步骤
     *
     * @param request
     * @return
     */
    StepExecuteResponse execute(ExecuteRequest request);

    /**
     * 单步执行
     *
     * @param request
     * @return
     */
    StepExecuteResponse singleExecute(ExecuteRequest request);

    /**
     * 批量执行步骤-弹窗步骤不支持
     *
     * @param request
     * @return
     */
    Boolean batchExecute(batchExecuteRequest request);

    /**
     * 保存序列配置
     *
     * @param testSequenceConfig
     * @return
     */
    Boolean saveTestSequenceConfig(TestSequenceConfig testSequenceConfig);

    /**
     * 查询序列配置
     *
     * @return
     */
    TestSequenceConfig getTestSequenceConfig();

    /**
     * 开始序列初始化
     *
     * @param request
     * @return
     */
    String startSequence(StartSequenceRequest request);


    /**
     * 结束序列
     *
     * @param mainSequenceId
     * @param childSequenceId
     * @param stepId
     * @return
     */
    String doneSequence(String mainSequenceId, String childSequenceId, String exceptVersion, String stepId);

    /**
     * 获取步骤模版
     *
     * @param stepType
     * @param subType
     * @return
     */
    StepVariable getStepStencil(String stepType, TestStepType subType);

    /**
     * 删除缓存
     *
     * @param testSequenceId
     */
    void removeCache(String testSequenceId);

    /**
     * 检查esn是否存在
     *
     * @param esn
     * @return
     */
    int checkEsn(String esn);

    /**
     * 查询场地列表
     *
     * @return
     */
    Object getSiteResponse();

    BaseJson validateTestSequence(String id);


    /**
     * 表达式语法校验
     *
     * @param expression
     * @return
     */
    BracketValidationResponse checkExpressionSyntax(String expression);

    /**
     * 复制序列
     *
     * @param id
     * @return
     */
    Boolean copyTestSequence(String id, String name, String newId);

    /**
     * 弹窗步骤信息展示返回
     * @param id
     * @return
     */
    List<InformationResponse> getInformation(String id);


}
