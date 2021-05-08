package com.alessio.soapconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoapConsumerApplication {

	public static void main(String[] args) {
		// example at: http://localhost:8085/Greeting/exampleWs
		SpringApplication.run(SoapConsumerApplication.class, args);
	}

}
