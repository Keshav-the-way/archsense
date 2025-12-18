package com.archsense.artifact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ArtifactServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtifactServiceApplication.class, args);
    }
}