package com.cetiti.service;

import com.cetiti.response.FunctionMetadataResponse;

import java.util.List;
import java.util.Map;

public interface ExpressionService {

    Map<String, List<FunctionMetadataResponse>> getFunctionMetadataList();

}
