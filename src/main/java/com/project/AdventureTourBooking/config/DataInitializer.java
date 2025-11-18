package com.project.AdventureTourBooking.config;

import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TourRepository tourRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User operator = userRepository.findByEmail("guide@adventure.com")
                .orElseGet(() -> createUser("guide@adventure.com", "Mountain Guide", UserRole.OPERATOR));

        if (tourRepository.count() == 0) {
            List<Tour> tours = List.of(
                    createTour(operator,
                            "Misty Mountain Hike",
                            "Start your morning above the clouds with a gentle hike to a hidden alpine lake.",
                            new BigDecimal("129.00"),
                            "Aspen, USA",
                            14,
                            5,
                            1),
                    createTour(operator,
                            "Rainforest River Kayak",
                            "Glide through lush mangroves while spotting parrots, sloths and river dolphins.",
                            new BigDecimal("189.00"),
                            "Leticia, Colombia",
                            10,
                            4,
                            2),
                    createTour(operator,
                            "Volcanic Sunset Jeep Ride",
                            "Bounce across black-sand trails before sharing a picnic on the caldera rim.",
                            new BigDecimal("159.00"),
                            "Santorini, Greece",
                            12,
                            3,
                            3),
                    createTour(operator,
                            "Nordic Fjord Cycling",
                            "Cycle quiet coastal roads, hop ferries between islands and taste local smoked salmon.",
                            new BigDecimal("210.00"),
                            "Ålesund, Norway",
                            8,
                            6,
                            4),
                    createTour(operator,
                            "Red Desert Stars",
                            "An overnight camel caravan with astronomer-led stargazing in the quiet dunes.",
                            new BigDecimal("275.00"),
                            "Merzouga, Morocco",
                            6,
                            12,
                            5)
            );
            tourRepository.saveAll(tours);
        }
    }

    private User createUser(String email, String name, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }

    private Tour createTour(User operator,
                            String title,
                            String description,
                            BigDecimal price,
                            String location,
                            int capacity,
                            int durationHours,
                            int weeksFromNow) {
        Tour tour = new Tour();
        tour.setOperator(operator);
        tour.setTitle(title);
        tour.setDescription(description);
        tour.setPrice(price);
        tour.setLocation(location);
        tour.setMaxCapacity(capacity);
        tour.setDurationHours(durationHours);
        tour.setAvailableFrom(LocalDateTime.now().plusWeeks(weeksFromNow));
        return tour;
    }
}
