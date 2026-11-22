package com.nexoralabs.navalhahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NavalhahubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NavalhahubApplication.class, args);
	}

}
