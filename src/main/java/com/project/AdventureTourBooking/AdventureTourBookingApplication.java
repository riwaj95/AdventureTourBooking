package com.project.AdventureTourBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AdventureTourBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdventureTourBookingApplication.class, args);
	}

}
