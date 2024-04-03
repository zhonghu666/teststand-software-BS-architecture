package com.cetiti.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestSequenceExecuteStatueDto implements Serializable {

    private String id;

    private Boolean runStatus;

    private String exceptVersion;
}
