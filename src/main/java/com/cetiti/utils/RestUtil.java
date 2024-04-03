package com.cetiti.utils;

import com.alibaba.fastjson.JSON;
import com.cetiti.entity.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import utils.entity.BusinessException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;


@Component
@Slf4j
public class RestUtil {

    @Resource
    private HttpServletRequest request;

    public RestResult getResultFromApi(String path, Object body, String param, HttpMethod method, String token) {
        try {
            String queryString = request.getQueryString();
            if (param != null) {
                queryString = queryString == null ? param : param + "&" + queryString;
            }
            URI uri = new URI(path);
            uri = fromUri(uri).query(queryString).build(true).toUri();
            log.info("rest call uri:{}", uri);
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.set(headerName, request.getHeader(headerName));
            }
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            RestResult result = restTemplate.exchange(uri, method, httpEntity, RestResult.class).getBody();
            log.info("result:{}", JSON.toJSONString(result));
            if (result == null || !result.isSuccess()) {
                throw new BusinessException(result == null ? "" : result.getMsg());
            }
            log.info("data:{}", JSON.toJSONString(result.getData()));
            return result;
        } catch (Exception e) {
            log.error(path + " api error:", e);
            throw new BusinessException("rest call error: " + e.getMessage());
        }
    }

}