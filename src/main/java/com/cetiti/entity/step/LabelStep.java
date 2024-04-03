package com.cetiti.entity.step;

import com.cetiti.constant.StepStatus;
import com.cetiti.entity.StepVariable;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("标签步骤")
public class LabelStep extends StepBase {

    @ApiModelProperty("描述")
    private String label;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        this.setDescribe(label);
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        step.addNestedAttribute("Label", label, "标签描述");
        return step;
    }
}
