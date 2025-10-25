package com.trustline.trustline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TrustlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrustlineApplication.class, args);
	}

}
