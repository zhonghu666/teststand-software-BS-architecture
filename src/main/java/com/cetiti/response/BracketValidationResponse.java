package com.cetiti.response;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class BracketValidationResponse implements Serializable {
    private boolean isValid;
    private List<ErrorMsg> errorMessages;
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

