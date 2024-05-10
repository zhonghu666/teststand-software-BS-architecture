package com.cetiti.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class BracketValidationResponse implements Serializable {
    @ApiModelProperty("是否通过")
    private boolean isValid;
    @ApiModelProperty("错误集合")
    private List<ErrorMsg> errorMessages;
    @ApiModelProperty("返回错误")
    private String returnErrorMsg;

    public BracketValidationResponse() {
        this.isValid = true;
        this.errorMessages = new ArrayList<>();
    }

    public void addError(String errorMessage, Integer errorStartPositions, Integer errorEndPositions) {
        if (isValid) { // If it's the first error, switch isValid to false
            isValid = false;
        }
        errorMessages.add(new ErrorMsg(errorMessage, errorStartPositions, errorEndPositions));
    }

    public static BracketValidationResponse valid() {
        return new BracketValidationResponse();
    }

    @Data
    class ErrorMsg implements Serializable {
        private String errorMessages;

        private Integer errorStartPositions;

        private Integer errorEndPositions;

        public ErrorMsg(String errorMessages, Integer errorStartPositions, Integer errorEndPositions) {
            this.errorMessages = errorMessages;
            this.errorStartPositions = errorStartPositions;
            this.errorEndPositions = errorEndPositions;
        }
    }
}

