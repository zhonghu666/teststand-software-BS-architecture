package com.cetiti.service.impl;

import com.cetiti.entity.FunctionMetadata;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.step.StepBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 获取StepVariable对象
    @Cacheable(value = "stepVariables", key = "#key", unless = "#result == null")
    public StepVariable getStepVariable(String key) {
        return (StepVariable) redisTemplate.opsForValue().get(key);
    }

    // 插入或更新StepVariable对象
    @CachePut(value = "stepVariables", key = "#key")
    public StepVariable saveOrUpdateStepVariable(String key, StepVariable stepVariable) {
        return stepVariable;
    }

    // 删除StepVariable对象
    @CacheEvict(value = "stepVariables", key = "#key")
    public void deleteStepVariable(String key) {
        redisTemplate.delete(key);
    }

    // 获取Step集合对象
    @Cacheable(value = "step", key = "#key", unless = "#result == null")
    public List<StepBase> getStep(String key) {
        return (List<StepBase>) redisTemplate.opsForValue().get(key);
    }

    // 插入或更新Step集合对象
    @CachePut(value = "step", key = "#key")
    public List<StepBase> saveOrUpdateStep(String key, List<StepBase> stepBaseList) {
        return stepBaseList;
    }

    // 删除StepVariable对象
    @CacheEvict(value = "step", key = "#key")
    public void deleteStep(String key) {
        redisTemplate.delete(key);
    }


    // 获取FunctionName集合对象
    @Cacheable(value = "FunctionName", key = "#key", unless = "#result == null")
    public List<String> getFunctionName(String key) {
        return (List<String>) redisTemplate.opsForValue().get(key);
    }

    // 插入或更新FunctionName集合对象
    @CachePut(value = "FunctionName", key = "#key")
    public List<String> saveOrUpdateFunctionName(String key, List<String> FunctionNameList) {
        return FunctionNameList;
    }

    // 删除FunctionName对象
    @CacheEvict(value = "FunctionName", key = "#key")
    public void deleteFunctionName(String key) {
        redisTemplate.delete(key);
    }

    @Cacheable(value = "FunctionMetadata", key = "#key", unless = "#result == null")
    public List<FunctionMetadata> getFunctionMetadata(String key) {
        return (List<FunctionMetadata>) redisTemplate.opsForValue().get(key);
    }

    // 插入或更新FunctionName集合对象
    @CachePut(value = "FunctionMetadata", key = "#key")
    public List<FunctionMetadata> saveOrUpdateFunctionMetadata(String key, List<FunctionMetadata> FunctionNameList) {
        return FunctionNameList;
    }

    // 删除FunctionName对象
    @CacheEvict(value = "FunctionMetadata", key = "#key")
    public void deleteFunctionMetadata(String key) {
        redisTemplate.delete(key);
    }
}

