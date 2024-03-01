package org.secretflow.secretpad.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class RestTemplateUtil {
    private static RestTemplate restTemplate = new RestTemplate();

    public static <T> T sendPostJson(String url, Object objReq, Map<String, String> headMap, Class<T> clazz) {
        ResponseEntity<String> stringResponseEntity = sendPostJson(url, objReq, headMap);
        return JsonUtils.toJavaObject(stringResponseEntity.getBody(), clazz);
    }

    public static ResponseEntity<String> sendPostJson(String url, Object objReq, Map<String, String> headMap) {
        log.debug("RestTemplateUtils.sendPostJson request: url={}, reqObj={}, headMap={}.", url, JsonUtils.toJSONString(objReq),
                JsonUtils.toJSONString(headMap));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (null != headMap) {
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity<String> httpEntity = new HttpEntity(JsonUtils.toJSONString(objReq), httpHeaders);
        ResponseEntity<String> tResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        log.debug("RestTemplateUtils.sendPostJson result: {}", tResponseEntity);
        return tResponseEntity;
    }


}
