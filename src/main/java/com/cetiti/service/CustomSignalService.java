package com.cetiti.service;

import com.cetiti.request.CustomSignalParesRequest;
import com.cetiti.request.CustomSignalRequest;
import com.cetiti.response.BracketValidationResponse;

public interface CustomSignalService {

    /**
     * 开始自定义信号
     *
     * @param request
     * @return
     */
    Boolean startCustomSignal(CustomSignalRequest request);

    /**
     * 表达式解析
     *
     * @param request
     * @return
     */
    BracketValidationResponse parseCustomSignal(CustomSignalParesRequest request);

    /**
     * 删除自定义表达式池数据
     *
     * @param name 表达式名称
     * @param uuid  数据回放id
     * @return
     */
    Boolean removeCustomSignal(String name, String uuid);

    /**
     * 自定义信号语法校验
     * @param request
     * @return
     */
    BracketValidationResponse checkCustomSignalSyntax(CustomSignalParesRequest request);
}
