package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CustomSignalRequest implements Serializable {

    private String esn;

    private Boolean start;

    private List<CustomSignalFieldRequest> customSignalFieldRequests;

    private String interval;
}
