package com.taghub.tag_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TagServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TagServerApplication.class, args);
	}

}
