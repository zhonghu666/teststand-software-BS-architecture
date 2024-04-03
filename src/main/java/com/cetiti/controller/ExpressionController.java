package com.cetiti.controller;

import com.cetiti.constant.BaseJson;
import com.cetiti.response.FunctionMetadataResponse;
import com.cetiti.service.ExpressionService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api("表达式管理")
@RestController
@RequestMapping("expression")
public class ExpressionController {

    @Resource
    private ExpressionService expressionService;

    @GetMapping("getExpressionTemplate")
    public BaseJson<Map<String, List<FunctionMetadataResponse>>> getFunctionMetadataList() {
        return new BaseJson<Map<String, List<FunctionMetadataResponse>>>().Success(expressionService.getFunctionMetadataList());
    }
}
