package com.archsense.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.archsense.executor",
        "com.archsense.common"
})
public class AnalysisExecutorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalysisExecutorApplication.class, args);
    }
}