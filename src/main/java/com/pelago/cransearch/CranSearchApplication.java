package com.pelago.cransearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CranSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(CranSearchApplication.class, args);
	}

}
