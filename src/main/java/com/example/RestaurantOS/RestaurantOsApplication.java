package com.example.RestaurantOS;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@OpenAPIDefinition(
//		info = @Info(
//				title = "FeignClient",
//				description = "FeignClient тест"
//		)
//)
@EnableFeignClients
public class RestaurantOsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantOsApplication.class, args);
	}

}
