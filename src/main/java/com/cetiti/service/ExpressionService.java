package com.cetiti.service;

import com.cetiti.response.FunctionMetadataResponse;

import java.util.List;
import java.util.Map;

public interface ExpressionService {
    /**
     * 获取函数元数据列表。
     * 从数据库中获取所有函数元数据，并按类型进行分组，然后按函数类型进行二级分组。
     *
     * @return 函数元数据列表的映射，键为函数类型，值为该类型下的函数元数据列表
     */
    Map<String, List<FunctionMetadataResponse>> getFunctionMetadataList();

}
