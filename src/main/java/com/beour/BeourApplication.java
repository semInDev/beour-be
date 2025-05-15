package com.beour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BeourApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeourApplication.class, args);
	}

}
