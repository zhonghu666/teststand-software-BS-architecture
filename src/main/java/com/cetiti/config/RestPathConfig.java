package com.cetiti.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.path")
@Data
public class RestPathConfig {

    private String baseApi;
    private String artificial;
    private String analysis;

}
