package com.archsense.executor.client;

import com.archsense.common.dto.response.ArtifactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArtifactServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ArtifactServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ArtifactServiceClient(RestTemplate restTemplate,
                                 @Value("${services.artifact.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ArtifactResponse getArtifactMetadata(String artifactId, String userId) {
        String url = baseUrl + "/api/artifacts/" + artifactId;
        log.info("Fetching artifact metadata for artifactId: {}", artifactId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<ArtifactResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ArtifactResponse.class
        );

        return response.getBody();
    }
}