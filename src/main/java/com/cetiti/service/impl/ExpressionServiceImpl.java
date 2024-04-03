package com.cetiti.service.impl;

import com.cetiti.entity.FunctionMetadata;
import com.cetiti.response.FunctionMetadataDetailsResponse;
import com.cetiti.response.FunctionMetadataResponse;
import com.cetiti.service.ExpressionService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpressionServiceImpl implements ExpressionService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Map<String, List<FunctionMetadataResponse>> getFunctionMetadataList() {
        List<FunctionMetadata> functionMetadataList = mongoTemplate.findAll(FunctionMetadata.class);
        Map<String, List<FunctionMetadataResponse>> response = functionMetadataList.stream()
                .collect(Collectors.groupingBy(FunctionMetadata::getType))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .collect(Collectors.groupingBy(FunctionMetadata::getFunctionType))
                                .entrySet()
                                .stream()
                                .map(functionTypeEntry -> {
                                    FunctionMetadataResponse functionMetadataResponse = new FunctionMetadataResponse();
                                    functionMetadataResponse.setType(functionTypeEntry.getKey());
                                    List<FunctionMetadataDetailsResponse> detailsResponses = functionTypeEntry.getValue()
                                            .stream()
                                            .map(functionMetadata -> {
                                                FunctionMetadataDetailsResponse f = new FunctionMetadataDetailsResponse();
                                                BeanUtils.copyProperties(functionMetadata, f);
                                                return f;
                                            })
                                            .collect(Collectors.toList());
                                    functionMetadataResponse.setFunctionMetadataDetailsResponses(detailsResponses);
                                    return functionMetadataResponse;
                                })
                                .collect(Collectors.toList())
                ));
        return response;
    }
}
