package com.identityos.onboarding_and_identity_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class
})
public class OnboardingAndIdentityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnboardingAndIdentityServiceApplication.class, args);
	}

}
