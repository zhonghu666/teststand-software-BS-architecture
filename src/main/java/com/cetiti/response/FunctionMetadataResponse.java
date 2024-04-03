package com.cetiti.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FunctionMetadataResponse implements Serializable {

    private String type;

    private List<FunctionMetadataDetailsResponse> functionMetadataDetailsResponses;

}
