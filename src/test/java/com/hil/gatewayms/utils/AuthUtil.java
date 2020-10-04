package com.hil.gatewayms.utils;

import java.net.URI;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class AuthUtil {

    private RestTemplate restTemplate;
    private URI apiUrl;

    public AuthUtil(RestTemplate restTemplate, URI apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public String getAuthToken() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("username","humans");
        body.put("password","hil123");

        HttpEntity<String> entity = new HttpEntity(body.toString(), createAuthHeaders());

        String response = restTemplate.postForObject(
            apiUrl.toString().concat("/api/auth/signin"),
            entity,
            String.class);
        return new JSONObject(response).getString("token");
    }

    private HttpHeaders createAuthHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public HttpHeaders createHeadersWithToken(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
