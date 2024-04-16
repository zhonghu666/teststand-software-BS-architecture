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
     * @param request
     * @return
     */
    BracketValidationResponse parseCustomSignal(CustomSignalParesRequest request);
}
