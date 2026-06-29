package com.outpass.outpass_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OutpassServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutpassServiceApplication.class, args);
	}

}
