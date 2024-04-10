package com.cetiti.entity.step;

import com.cetiti.constant.StepStatus;
import com.cetiti.entity.StepVariable;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class WaitStep extends StepBase implements Serializable {

    @ApiModelProperty("等待时间")
    @NotNull(message = "等待时间不能为空")
    private Long waitTime;

    @Override
    public StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        if (pram != null && pram.get("executeType") != null) {
            try {
                Thread.sleep(waitTime*1000);
            } catch (Exception e) {
                log.error("子序列-wait步骤报错", e);
            }

        }
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        step.addNestedAttribute("TimeoutExpr", waitTime, "等待时间");
        return step;
    }
}
