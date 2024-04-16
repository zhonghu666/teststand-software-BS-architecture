package com.cetiti.controller;

import com.cetiti.constant.BaseJson;
import com.cetiti.request.CustomSignalParesRequest;
import com.cetiti.request.CustomSignalRequest;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.CustomSignalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api("自定义信号管理")
@RestController
@RequestMapping("customSignal")
public class CustomSignalController {

    @Resource
    private CustomSignalService customSignalService;

    @ApiOperation(value = "开始/停止自定义信号数据同步")
    @PostMapping("start")
    public BaseJson<Boolean> startCustomSignal(@RequestBody CustomSignalRequest customSignalRequest) {
        return new BaseJson<Boolean>().Success(customSignalService.startCustomSignal(customSignalRequest));
    }

    @ApiOperation(value = "解析自定义信号表达式")
    @PostMapping("parse")
    public BaseJson<BracketValidationResponse> parseCustomSignal(@RequestBody CustomSignalParesRequest request) {
        return new BaseJson<BracketValidationResponse>().Success(customSignalService.parseCustomSignal(request));
    }
}
