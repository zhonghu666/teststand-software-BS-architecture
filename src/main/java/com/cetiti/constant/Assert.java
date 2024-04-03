package com.cetiti.constant;

import lombok.extern.slf4j.Slf4j;
import utils.entity.BusinessException;
import utils.entity.InvalidDataException;

@Slf4j
public class Assert {
    public Assert() {
    }

    public static void handle(Boolean judge, String exceptionMsg, String code) {
        if (!judge) {
            log.error("错误信息:{}", exceptionMsg);
            throw new BusinessException(code, exceptionMsg);
        }
    }

    public static void handle(Boolean judge, String exceptionMsg) {
        if (!judge) {
            log.error("错误信息:{}", exceptionMsg);
            throw new BusinessException("500", exceptionMsg);
        }
    }

    public static void dataHandle(Boolean judge, String exceptionMsg) {
        if (!judge) {
            log.error("错误信息:{}", exceptionMsg);
            throw new InvalidDataException("510", exceptionMsg);
        }
    }
}
