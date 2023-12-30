package com.Star.Star;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		boolean testOnBackend = true;
		if (testOnBackend) {
			try {
				new BatchRun().testBatchRun(1000, 2, 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			SpringApplication.run(Application.class, args);
		}
	}

}
