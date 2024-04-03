package com.cetiti.annotation;

import com.cetiti.constant.ActionType;
import com.cetiti.constant.DeviceType;
import com.cetiti.entity.step.ActionStep;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ActionValidator implements ConstraintValidator<ActionValid, ActionStep> {


    @Override
    public void initialize(ActionValid constraintAnnotation) {
//        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(ActionStep actionStep, ConstraintValidatorContext constraintValidatorContext) {

        if (Objects.equals(actionStep.getActionType(), ActionType.CONFIGURATION)) {
            if (Objects.isNull(actionStep.getDeviceType())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("设备类型不能为空").addConstraintViolation();
                return false;
            }
            if (Objects.isNull(actionStep.getEsn())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("设备编号不能为空").addConstraintViolation();
                return false;
            }
            if (Objects.equals(actionStep.getDeviceType(), DeviceType.RSU)) {
                if (actionStep.getRsuBroadcastDto() == null && actionStep.getRsuCfgDto() == null && actionStep.getRsuSceneConfigDto() == null) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext.buildConstraintViolationWithTemplate("rsu配置信息不能全部为空").addConstraintViolation();
                    return false;
                }
            }

        } else if (Objects.equals(actionStep.getActionType(), ActionType.SCENE)) {
            if (Objects.isNull(actionStep.getChildrenType())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("子类型不能为空").addConstraintViolation();
                return false;
            }
            if (Objects.isNull(actionStep.getSceneId())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("场景编号不能为空").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
