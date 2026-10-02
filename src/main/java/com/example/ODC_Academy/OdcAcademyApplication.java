package com.example.ODC_Academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableAsync
@SpringBootApplication
public class OdcAcademyApplication {

	public static void main(String[] args) {
		SpringApplication.run(OdcAcademyApplication.class, args);
	}

}
