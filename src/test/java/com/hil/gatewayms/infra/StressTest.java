package com.hil.gatewayms.infra;

import static org.testng.Assert.fail;

import com.hil.gatewayms.utils.AuthUtil;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StressTest {

    private final URI apiUrl = URI.create("http://localhost:8080");

    private RestTemplate restTemplate;
    private SecureRandom secureRandom;
    private AuthUtil authUtil;

    @BeforeClass
    void setUp() {
        restTemplate = new RestTemplate();
        secureRandom = new SecureRandom();

        authUtil = new AuthUtil(restTemplate, apiUrl);
    }

    @Test
    public void testMultipleProductCreateSingleUser() throws JSONException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        String token = authUtil.getAuthToken();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Runnable worker = () -> {
                try {

                    String randomID = String.valueOf(secureRandom.nextInt());
                    JSONObject data = new JSONObject();
                    data.put("name", "Prod ".concat(randomID));
                    data.put("description", "Description ".concat(randomID));
                    data.put("price", Double.valueOf(randomID));

                    HttpEntity<String> entity = new HttpEntity(data.toString(), authUtil.createHeadersWithToken(token));
                    ResponseEntity<String> rez = restTemplate.postForEntity("http://localhost:8080/gateway/data/product/create", entity, String.class);
                    System.out.println(rez.getBody());

                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            };
            executor.execute(worker);
        }
        //1 -> t1
        //2 -> t1 running, t2(2)
        //11 -> t1 run, t2, run, ...t10 run
        //11 -> t1 run, ... t10 done
        //12 -> t1 run, ... t10 (11)

        executor.shutdown();
        executor.awaitTermination(120, TimeUnit.SECONDS);

        long end = System.currentTimeMillis();

        System.out.println(end - start);
    }

    @Test
    public void testMultipleUserSingleSigninCall() throws JSONException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(100);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Runnable worker = () -> {
                try {

                    String token = authUtil.getAuthToken();
                    System.out.println(token);

                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            };
            executor.execute(worker);
        }
        //1 -> t1
        //2 -> t1 running, t2(2)
        //11 -> t1 run, t2, run, ...t10 run
        //11 -> t1 run, ... t10 done
        //12 -> t1 run, ... t10 (11)

        executor.shutdown();
        executor.awaitTermination(120, TimeUnit.SECONDS);

        long end = System.currentTimeMillis();

        System.out.println(end - start);
    }
}
