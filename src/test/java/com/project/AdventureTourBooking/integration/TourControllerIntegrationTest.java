package com.project.AdventureTourBooking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import com.project.AdventureTourBooking.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TourControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        tourRepository.deleteAll();
        userRepository.deleteAll();
    }
/*
    @Test
    void getAllTours_returnsPersistedTours() throws Exception {
        User operator = persistOperator("operator@example.com");

        Tour canyonTour = new Tour();
        canyonTour.setOperator(operator);
        canyonTour.setTitle("Canyon Expedition");
        canyonTour.setDescription("Explore the canyon");
        canyonTour.setPrice(BigDecimal.valueOf(199.99));
        canyonTour.setLocation("Utah");
        canyonTour.setMaxCapacity(10);
        canyonTour.setAvailableFrom(LocalDateTime.now().plusDays(10));
        canyonTour.setDurationHours(6);
        tourRepository.save(canyonTour);

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Canyon Expedition"))
                .andExpect(jsonPath("$[0].operatorId").value(operator.getId()));
    }*/

    @Test
    void createTour_withOperatorAuthentication_persistsTour() throws Exception {
        User operator = persistOperator("guide@example.com");

        TourRequest request = new TourRequest(
                "River Rafting",
                "Thrilling river adventure",
                BigDecimal.valueOf(150.00),
                "Colorado",
                12,
                LocalDateTime.now().plusDays(5),
                4
        );

        mockMvc.perform(post("/api/tours")
                        .with(authenticate(operator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/tours/\\d+")))
                .andExpect(jsonPath("$.title").value("River Rafting"))
                .andExpect(jsonPath("$.operatorId").value(operator.getId()));

        assertThat(tourRepository.findAll()).hasSize(1);
        assertThat(tourRepository.findAll().get(0).getTitle()).isEqualTo("River Rafting");
    }

    @Test
    void getToursByOperator_withMismatchedPrincipal_returnsForbidden() throws Exception {
        User owner = persistOperator("owner@example.com");
        User otherOperator = persistOperator("other@example.com");

        Tour ownedTour = new Tour();
        ownedTour.setOperator(owner);
        ownedTour.setTitle("Desert Safari");
        ownedTour.setDescription("Ride through the dunes");
        ownedTour.setPrice(BigDecimal.valueOf(220.00));
        ownedTour.setLocation("Nevada");
        ownedTour.setMaxCapacity(8);
        ownedTour.setAvailableFrom(LocalDateTime.now().plusDays(15));
        ownedTour.setDurationHours(5);
        tourRepository.save(ownedTour);

        mockMvc.perform(get("/api/tours/operators/" + owner.getId())
                        .with(authenticate(otherOperator)))
                .andExpect(status().isForbidden());
    }

    private User persistOperator(String email) {
        User operator = new User();
        operator.setName("Operator " + email);
        operator.setEmail(email);
        operator.setPassword(passwordEncoder.encode("password"));
        operator.setRole(UserRole.OPERATOR);
        return userRepository.save(operator);
    }

    private RequestPostProcessor authenticate(User user) {
        return user(new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole()));
    }
}
