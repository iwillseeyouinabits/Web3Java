package com.Star.Star;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws Exception {
		
		// new BatchRun().testBatchRun(2000, 2, 2);
		SpringApplication.run(Application.class, args);
	}

}
