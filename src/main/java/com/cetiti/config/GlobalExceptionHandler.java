package com.cetiti.config;

import com.cetiti.constant.BaseJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import utils.entity.BusinessException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 静态资源版本问题处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseJson<?>> handleBusinessException(BusinessException ex) {
        log.error("业务异常: {}", ex.getErrorMsg());
        BaseJson<Object> baseJson = new BaseJson<>().Fail(ex.getErrorMsg(), ex.getErrorCode());
        return new ResponseEntity<>(baseJson, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseJson<?>> handleAllExceptions(Exception ex) {
        log.error("系统异常: ", ex); // 完整异常栈的打印
        BaseJson<Object> baseJson = new BaseJson<>().Fail("系统错误，请联系管理员", ex.getMessage());
        return new ResponseEntity<>(baseJson, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseJson<?>> handleBindExceptions(MethodArgumentNotValidException ex) {
        log.error("校验异常: ", ex);
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        BaseJson<Object> baseJson = new BaseJson<>();
        baseJson.Fail("验证失败", errors); // 注意调整Fail方法使其能接受List<String>作为错误信息
        return new ResponseEntity<>(baseJson, HttpStatus.BAD_REQUEST);
    }

}
