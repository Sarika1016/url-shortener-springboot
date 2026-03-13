package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the URL Shortener application.
 *
 * @SpringBootApplication combines:
 *   - @Configuration       : marks this as a Spring config source
 *   - @EnableAutoConfiguration : auto-configures Spring Boot
 *   - @ComponentScan       : scans all sub-packages for beans
 */
@SpringBootApplication
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
