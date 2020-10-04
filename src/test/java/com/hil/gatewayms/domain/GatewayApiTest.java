package com.hil.gatewayms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.hil.gatewayms.utils.AuthUtil;
import java.net.URI;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
@TestPropertySource(locations = "/application-test.yml")
public class GatewayApiTest {

    private final URI apiUrl = URI.create("http://localhost:8080");
    private RestTemplate restTemplate;
    private AuthUtil authUtil;

    @Value("${gateway.api.username}")
    private String username;

    @Value("${gateway.api.password}")
    private String password;

    @BeforeClass
    void setUp() {
        restTemplate = new RestTemplate();
        authUtil = new AuthUtil(restTemplate, apiUrl);
    }

    @Test
    public void shouldFailForMissingToken() {
        try {
            restTemplate.getForEntity(apiUrl.toString().concat("/gateway/data/product/all"), Object.class);
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test
    //@Disabled("auth throws 400 BAD REQUEST")
    public void shouldPassForTokenCorrectlyAttached() throws JSONException {
        String token = authUtil.getAuthToken();

        try {
            HttpEntity<String> entity = new HttpEntity(null, authUtil.createHeadersWithToken(token));
            ResponseEntity<String> response = restTemplate.exchange(apiUrl.toString().concat("/gateway/data/product/all"),
                HttpMethod.GET,
                entity,
                String.class);
            assertNotNull(response.getBody());

            // test response entities structure, not one attribute more than needed client side -> contract -> API doc
            // prodid, prodname, prodcost

            JSONObject jsonObject = new JSONObject(response.getBody());
            assertNotNull(jsonObject.getString("prodid"));
            assertNotNull(jsonObject.getString("prodname"));
            assertNotNull(jsonObject.getString("prodcost"));
            assertEquals(jsonObject.keys(), 3);

            // Validate headers - security

            // shared library, data model - ProductDto
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }
}