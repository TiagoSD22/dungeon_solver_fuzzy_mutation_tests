package com.dungeon;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pholser.junit.quickcheck.From;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.dungeon.generators.RectIntMatrixGenerator;
import com.dungeon.generators.RandomJsonGenerator;
import com.dungeon.generators.MalformedJsonGenerator;
import java.util.Map;

@RunWith(JQF.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MatrixFuzzTest {

    // Fuzz target that sends arbitrary JSON values under the "input" key
    @Fuzz
    public void fuzzMatrixInput(@From(RandomJsonGenerator.class) String input) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";
            String jsonBody = "{\"input\": " + input + "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());

        }
    }

    // Fuzz target that sends a full random JSON body (no fixed schema)
    @Fuzz
    public void fuzzRawJsonBody(@From(RandomJsonGenerator.class) String body) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    // Fuzz target that sends intentionally malformed JSON as the entire body
    @Fuzz
    public void fuzzMalformedJsonBody(@From(MalformedJsonGenerator.class) String body) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            // Expect parse or 4xx errors here; still useful signal
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
