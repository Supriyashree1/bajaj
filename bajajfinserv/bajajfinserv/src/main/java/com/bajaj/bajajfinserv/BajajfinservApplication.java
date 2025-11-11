package com.bajaj.bajajfinserv;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajfinservApplicationTests implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String name = "Supriyashree I R";
    private final String regNo = "U25UV22T029070";
    private final String email = "supriyashree.ir@campusuvce.in";

    public static void main(String[] args) {
        SpringApplication.run(BajajfinservApplicationTests .class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Bajaj Finserv task...");

        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("regNo", regNo);
        requestBody.put("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(generateUrl, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("Failed to generate webhook. Status: " + response.getStatusCode());
            System.err.println("Body: " + response.getBody());
            return;
        }

        JsonNode json = mapper.readTree(response.getBody());
        String webhook = json.path("webhook").asText();
        String accessToken = json.path("accessToken").asText();

        System.out.println("Webhook received: " + webhook);
        System.out.println("Access token received.");

        String finalSqlQuery = getFinalSqlQuery();

        if (finalSqlQuery == null || finalSqlQuery.trim().isEmpty()) {
            System.err.println("Please fill in your final SQL query inside getFinalSqlQuery()");
            return;
        }

        String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        HttpHeaders submitHeaders = new HttpHeaders();
        submitHeaders.setContentType(MediaType.APPLICATION_JSON);
        submitHeaders.set("Authorization", accessToken);

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalSqlQuery);

        HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(body, submitHeaders);
        ResponseEntity<String> submitResponse = restTemplate.postForEntity(submitUrl, submitEntity, String.class);

        System.out.println("Submission Status: " + submitResponse.getStatusCode());
        System.out.println("Response Body: " + submitResponse.getBody());
    }

    private String getFinalSqlQuery() {
        return "/* SELECT \n" +
                "    e1.EMP_ID,\n" +
                "    e1.FIRST_NAME,\n" +
                "    e1.LAST_NAME,\n" +
                "    d.DEPARTMENT_NAME,\n" +
                "    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT\n" +
                "FROM EMPLOYEE e1\n" +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID\n" +
                "LEFT JOIN EMPLOYEE e2 \n" +
                "    ON e1.DEPARTMENT = e2.DEPARTMENT      -- same department\n" +
                "    AND e2.DOB > e1.DOB                   -- e2 is younger than e1\n" +
                "GROUP BY \n" +
                "    e1.EMP_ID,\n" +
                "    e1.FIRST_NAME,\n" +
                "    e1.LAST_NAME,\n" +
                "    d.DEPARTMENT_NAME\n" +
                "ORDER BY \n" +
                "    e1.EMP_ID DESC; */";
    }
}
