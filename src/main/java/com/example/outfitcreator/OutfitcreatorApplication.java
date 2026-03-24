package com.example.outfitcreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the OutfitCreator Spring Boot application.
 * <p>
 * Boots the web stack, JPA, security, caching, and OpenAPI as configured in the classpath.
 */
@SpringBootApplication
public class OutfitcreatorApplication {

	/**
	 * Starts the embedded web server and Spring application context.
	 *
	 * @param args standard Spring Boot command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(OutfitcreatorApplication.class, args);
	}

}
