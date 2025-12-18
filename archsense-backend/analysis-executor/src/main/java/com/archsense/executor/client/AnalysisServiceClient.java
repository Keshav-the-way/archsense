package com.archsense.executor.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AnalysisServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AnalysisServiceClient(RestTemplate restTemplate,
                                 @Value("${services.analysis.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void updateStatusInProgress(String analysisId) {
        String url = baseUrl + "/internal/analyses/" + analysisId + "/status/in-progress";
        log.info("Updating analysis status to IN_PROGRESS: {}", analysisId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

            log.info("Successfully updated status to IN_PROGRESS for analysisId: {}", analysisId);

        } catch (Exception e) {
            log.error("Failed to update analysis status", e);
        }
    }
}