package com.cetiti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger3 {

    @Value("${app.version}")
    private String version;

    @Value("${swagger.host}")
    private String swaggerHost;

    @Value("${swagger.enabled}")
    private Boolean enable;

    @Bean
    public Docket desertsApi1() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo("国汽自动测试序列"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.cetiti.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("测试序列")
                .enable(enable)
                .host(swaggerHost);
    }


    private ApiInfo apiInfo(String title) {
        return new ApiInfoBuilder()
                .title(title)
                .description("国汽自动测试序列")
                .contact(new Contact("黄昶", "", ""))
                .termsOfServiceUrl("")
                .version(version)
                .build();
    }
}
