package com.dungeon;

import com.dungeon.fuzz.DungeonMatrixGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.pholser.junit.quickcheck.From;
import org.junit.Assert;

@RunWith(JQF.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MatrixFuzzTest {

    @Fuzz
    public void fuzzMatrixInput(@From(DungeonMatrixGenerator.class) String matrixJson) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/dungeon/solve";
            String jsonBody = "{\"input\": " + matrixJson + "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // Assert: No 5xx errors, response body is not null
            Assert.assertNotNull("Response body should not be null", response.getBody());
            Assert.assertTrue("Should not return 5xx error", response.getStatusCode().value() < 500);

        } catch (Exception e) {
            // Optionally: Assert that exception is not a server error
            Assert.assertFalse("Should not throw server error", e.getMessage().contains("500"));
        }
    }
}
