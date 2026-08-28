package com.evermynd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EverMyndApplication {
	public static void main(String[] args) {
		SpringApplication.run(EverMyndApplication.class, args);
	}
}