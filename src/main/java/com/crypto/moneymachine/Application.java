package com.crypto.moneymachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.crypto.moneymachine.*"})
@EntityScan(basePackages = {"com.crypto.moneymachine.*"})
//@EnableJpaRepositories(basePackages = {"com.crypto.moneymachine.*"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
