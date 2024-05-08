package com.cetiti.entity.step;

import com.cetiti.constant.StepStatus;
import com.cetiti.entity.StepVariable;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class PopupStep extends StepBase implements Serializable {

    @ApiModelProperty("标题")
    @NotNull(message = "标题不能为空")
    private String textTitle;

    @ApiModelProperty("内容")
    private String message;

    @ApiModelProperty("按钮集合")
    private List<Button> buttons;

    @ApiModelProperty("焦点按钮id")
    private Integer focusButtonId;

    @ApiModelProperty("超时按钮id")
    private Integer timeOutButton;

    @ApiModelProperty("超时时间")
    private Integer timeOut;

    @ApiModelProperty("应答文本")
    private List<ReplyText> replyTexts;

    @ApiModelProperty("信息展示")
    private List<information> informationDisplay;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        StepVariable stepVariable = cacheService.getStepVariable(getTestSequenceId());
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        step.addNestedAttribute("MessageExpr", message != null ? message : "", "内容");
        step.addNestedAttribute("TitleExpr", textTitle, "标题");
        buttons.forEach(i -> {
            step.addNestedAttribute("ButtonLabel" + i.getId(), i.getName(), "按键内容");
        });
        pram.forEach((key, value) -> {
            if (!key.equals("chooseButton") && !key.equals("DATA_CALL_TOPIC")) {
                step.addNestedAttributeObject("replyText." + key, value, key);
            }
        });
        int flag = 1;
        for (information information : informationDisplay) {
            Map<String, Object> map = ExpressionParserUtils.expressionParsingExecution(information.getInformation(), stepVariable, cacheService, testSequenceId);
            step.addNestedAttribute("Result.Information" + flag, map.get("result"), information.getKey());
        }
        if (timeOut != null) {
            step.addNestedAttribute("TimeOut", timeOut, "超时时间");
        }
        Integer chooseButtonId = (Integer) pram.get("chooseButton");
        Button button = buttons.stream().filter(i -> Objects.equals(i.getId(), chooseButtonId)).findFirst().get();
        step.addNestedAttribute("ChooseButton", button.getName(), "选择按钮");
        return step;
    }

    @Data
    static class Button implements Serializable {
        private Integer id;
        private String name;
    }

    @Data
    public static class information implements Serializable {
        private String key;

        private String information;
    }

    @Data
    public static class ReplyText<T> {
        private String key;
        private String title;
        private ValueType type;
        private T defaultResponse;

        private ReplyText(String key, String title, ValueType type, T value) {
            this.type = type;
            this.defaultResponse = value;
        }

        public ReplyText() {
        }

        public static <T> ReplyText<T> of(String key, String title, T defaultResponse) {
            if (defaultResponse instanceof String) {
                return new ReplyText<>(key, title, ValueType.STRING, defaultResponse);
            } else if (defaultResponse instanceof Number) {
                return new ReplyText<>(key, title, ValueType.NUMBER, defaultResponse);
            } else if (defaultResponse instanceof Boolean) {
                return new ReplyText<>(key, title, ValueType.BOOLEAN, defaultResponse);
            } else if (defaultResponse instanceof LocalDateTime) {
                return new ReplyText<>(key, title, ValueType.LOCAL_DATE_TIME, defaultResponse);
            } else {
                throw new IllegalArgumentException("Unsupported type");
            }
        }
    }

    public enum ValueType {
        STRING, NUMBER, BOOLEAN, LOCAL_DATE_TIME, DATE
    }
}
