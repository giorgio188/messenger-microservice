package com.messenger.presence_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
exclude = {
DataSourceAutoConfiguration.class,
HibernateJpaAutoConfiguration.class})
@EnableScheduling
public class PresenceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PresenceServiceApplication.class, args);
	}

}
