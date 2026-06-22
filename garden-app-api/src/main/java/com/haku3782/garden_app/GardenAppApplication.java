package com.haku3782.garden_app;

import com.haku3782.garden_app.config.FlywayMigrationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GardenAppApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GardenAppApplication.class);
		app.addListeners(new FlywayMigrationListener());
		app.run(args);
	}

}
