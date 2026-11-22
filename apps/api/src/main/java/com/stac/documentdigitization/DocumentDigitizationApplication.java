package com.stac.documentdigitization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Starts the API and its persistence configuration. */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
public class DocumentDigitizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentDigitizationApplication.class, args);
    }
}
