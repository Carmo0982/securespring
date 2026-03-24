package edu.eci.tdse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/secure")
public class SecureApiController {

    private final RestClient restClient;
    private final String apacheDownloadUrl;

    public SecureApiController(
            RestClient.Builder restClientBuilder,
            @Value("${app.apache.download-url}") String apacheDownloadUrl) {
        this.restClient = restClientBuilder.build();
        this.apacheDownloadUrl = apacheDownloadUrl;
    }

    @GetMapping("/hello")
    public Map<String, String> secureHello() {
        return Map.of("message", "Authenticated access granted");
    }

    @GetMapping("/apache-check")
    public Map<String, Object> checkApacheTls() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("target", apacheDownloadUrl);

        try {
            String body = restClient.get()
                    .uri(apacheDownloadUrl)
                    .retrieve()
                    .body(String.class);

            response.put("status", HttpStatus.OK.value());
            response.put("tlsRequest", "success");
            response.put("preview", body == null ? "" : body.substring(0, Math.min(body.length(), 120)));
            return response;
        } catch (RestClientException ex) {
            response.put("status", HttpStatus.BAD_GATEWAY.value());
            response.put("tlsRequest", "failed");
            response.put("error", ex.getMessage());
            return response;
        }
    }
}
