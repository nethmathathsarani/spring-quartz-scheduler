package com.test.demo;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@EnableTransactionManagement
@SpringBootApplication
public class SpringQuartzExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringQuartzExampleApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setReadTimeout(600000);
		requestFactory.setConnectTimeout(600000);
		return new RestTemplate(requestFactory);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}





}
