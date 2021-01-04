package com.example.csvman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class CsvmanApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(CsvmanApplication.class, args)));
	}

}
