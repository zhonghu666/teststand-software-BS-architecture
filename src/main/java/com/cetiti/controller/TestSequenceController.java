package com.cetiti.controller;

import com.cetiti.constant.*;
import com.cetiti.entity.StepVariable;

import com.cetiti.entity.TestSequenceConfig;
import com.cetiti.request.*;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.response.InformationResponse;
import com.cetiti.response.StepExecuteResponse;
import com.cetiti.response.TestSequenceResponse;
import com.cetiti.service.TestSequenceService;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Api("测试序列管理")
@RestController
@RequestMapping("testSequence")
public class TestSequenceController {

    @Resource
    private TestSequenceService testSequenceService;

    @Resource
    private CacheService cacheService;

    @ApiOperation(value = "保存序列")
    @PostMapping("/save")
    public BaseJson saveTestSequence(@RequestBody @Valid TestSequenceSaveRequest request, BindingResult result) {
        if (result.hasErrors()) {
            List<ValidationError> validationErrors = result.getFieldErrors().stream().map(fieldError -> new ValidationError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage())
            ).collect(Collectors.toList());
            // 返回错误信息列表
            return new BaseJson().Fail("参数错误", ResponseEntity.badRequest().body(validationErrors));
        }
        return new BaseJson().Success(testSequenceService.saveTestSequence(request));
    }

    @ApiOperation(value = "修改序列名称")
    @PostMapping("/editName")
    public BaseJson<String> editTestSequenceName(@RequestBody TestSequenceEditRequest request) {
        return new BaseJson<String>().Success(testSequenceService.editTestSequenceName(request.getId(), request.getSequenceName()));
    }

    @ApiOperation(value = "查询序列列表")
    @GetMapping("/queryList")
    public BaseJson<List<TestSequenceResponse>> getTestSequenceAll(@RequestHeader(name = "token") String token) {
        return new BaseJson<List<TestSequenceResponse>>().Success(testSequenceService.getTestSequenceAll(token));
    }

    @ApiOperation(value = "根据id查询序列")
    @GetMapping("getSequenceDetails")
    public BaseJson<TestSequenceResponse> getTestSequenceById(@RequestParam String id) {
        return new BaseJson<TestSequenceResponse>().Success(testSequenceService.getTestSequenceById(id));
    }

    @ApiOperation(value = "删除序列")
    @DeleteMapping("removeSequence")
    public BaseJson<Boolean> removeTestSequence(String id) {
        return new BaseJson<Boolean>().Success(testSequenceService.removeTestSequence(id));
    }

    @ApiOperation(value = "步骤执行")
    @PostMapping("execute")
    public BaseJson<StepExecuteResponse> execute(@RequestBody ExecuteRequest request) {
        return new BaseJson<StepExecuteResponse>().Success(testSequenceService.execute(request));
    }

    @ApiOperation(value = "单步骤执行")
    @PostMapping("singleExecute")
    public BaseJson<StepExecuteResponse> singleExecute(@RequestBody ExecuteRequest request) {
        return new BaseJson<StepExecuteResponse>().Success(testSequenceService.singleExecute(request));
    }

    @ApiOperation(value = "保存序列配置")
    @PostMapping("saveTestSequenceConfig")
    public BaseJson<Boolean> saveTestSequenceConfig(@RequestBody TestSequenceConfig testSequenceConfig) {
        return new BaseJson<Boolean>().Success(testSequenceService.saveTestSequenceConfig(testSequenceConfig));
    }

    @ApiOperation(value = "获取序列配置")
    @GetMapping("getTestSequenceConfig")
    public BaseJson<TestSequenceConfig> getTestSequenceConfig() {
        return new BaseJson<TestSequenceConfig>().Success(testSequenceService.getTestSequenceConfig());
    }

    @ApiOperation(value = "步骤结果模版")
    @GetMapping("stepStencil")
    public BaseJson<StepVariable> getStepStencil(@RequestParam String stepType, @RequestParam(required = false) TestStepType subType) {
        return new BaseJson<StepVariable>().Success(testSequenceService.getStepStencil(stepType, subType));
    }

    @ApiOperation(value = "开始序列初始化")
    @PostMapping("startSequence")
    public BaseJson<String> startSequence(@RequestBody StartSequenceRequest request) {
        return new BaseJson<String>().Success(testSequenceService.startSequence(request));
    }

    @ApiOperation(value = "结束序列")
    @GetMapping("doneSequence")
    public BaseJson<String> doneSequence(@RequestParam String mainSequenceId, @RequestParam(required = false) String childSequenceId, @RequestParam(required = false) String exceptVersion) {
        return new BaseJson<String>().Success(testSequenceService.doneSequence(mainSequenceId, childSequenceId, exceptVersion));
    }

    @ApiOperation(value = "批量执行步骤")
    @PostMapping("batchExecute")
    public BaseJson<Boolean> batchExecute(@RequestBody batchExecuteRequest request) {
        return new BaseJson<Boolean>().Success(testSequenceService.batchExecute(request));
    }

    @ApiOperation(value = "检查esn是否在序列内")
    @GetMapping("checkEsn")
    public BaseJson<Integer> checkEsn(@RequestParam String esn) {
        return new BaseJson<Integer>().Success(testSequenceService.checkEsn(esn));
    }

    @ApiOperation(value = "获取场地列表")
    @GetMapping("getSiteList")
    public BaseJson<Object> getSiteList() {
        return new BaseJson<>().Success(testSequenceService.getSiteResponse());
    }

    @ApiOperation(value = "检验序列参数")
    @GetMapping("validateTestSequence")
    public BaseJson validateTestSequence(@RequestParam String id) {
        return testSequenceService.validateTestSequence(id);
    }

    @ApiOperation(value = "表达式语法检验")
    @PostMapping("checkExpressionSyntax")
    public BaseJson<BracketValidationResponse> checkExpressionSyntax(@RequestBody String expression) {
        return new BaseJson<BracketValidationResponse>().Success(testSequenceService.checkExpressionSyntax(expression));
    }

    @ApiOperation(value = "复制序列")
    @GetMapping("copyTestSequence")
    public BaseJson<Boolean> copyTestSequence(@RequestParam String id, @RequestParam String name, @RequestParam String newId) {
        return new BaseJson<Boolean>().Success(testSequenceService.copyTestSequence(id, name, newId));
    }

    @ApiOperation(value = "弹窗步骤信息展示返回")
    @GetMapping("getInformation")
    public BaseJson<List<InformationResponse>> getInformation(@RequestParam String id) {
        return new BaseJson<List<InformationResponse>>().Success(testSequenceService.getInformation(id));
    }

    @ApiOperation(value = "获取序列变量树")
    @GetMapping("getStepVariableById")
    public BaseJson<StepVariable> getStepVariableById(@RequestParam String id) {
        return new BaseJson<StepVariable>().Success(cacheService.getStepVariable(id));
    }
}
