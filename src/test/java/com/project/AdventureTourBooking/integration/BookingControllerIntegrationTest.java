package com.project.AdventureTourBooking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.dto.BookingStatusUpdateRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.BookingRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        tourRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBooking_withCustomerAuthentication_persistsBooking() throws Exception {
        User operator = persistOperator("operator@example.com");
        Tour tour = persistTour(operator, "Mountain Hiking", BigDecimal.valueOf(180.00));
        User customer = persistCustomer("customer@example.com");

        BookingRequest request = new BookingRequest(
                tour.getId(),
                3,
                BigDecimal.valueOf(540.00),
                LocalDateTime.now().plusDays(2),
                null
        );

        mockMvc.perform(post("/api/bookings")
                        .with(authenticate(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.numberOfPeople").value(3));

        assertThat(bookingRepository.findAll()).hasSize(1);
        Booking saved = bookingRepository.findAll().get(0);
        assertThat(saved.getTour().getId()).isEqualTo(tour.getId());
        assertThat(saved.getUser().getId()).isEqualTo(customer.getId());
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void getBookingsForOperator_returnsOnlyOwnedBookings() throws Exception {
        User operator = persistOperator("operator@example.com");
        Tour tour = persistTour(operator, "City Tour", BigDecimal.valueOf(99.99));
        User customer = persistCustomer("customer@example.com");

        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setUser(customer);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setNumberOfPeople(2);
        booking.setTotalPrice(BigDecimal.valueOf(199.98));
        booking.setBookingDate(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        mockMvc.perform(get("/api/bookings/operator")
                        .with(authenticate(operator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void updateBookingStatus_withOperatorAuthentication_updatesStatus() throws Exception {
        User operator = persistOperator("operator@example.com");
        Tour tour = persistTour(operator, "Forest Camping", BigDecimal.valueOf(250.00));
        User customer = persistCustomer("customer@example.com");

        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setUser(customer);
        booking.setStatus(BookingStatus.PENDING);
        booking.setNumberOfPeople(4);
        booking.setTotalPrice(BigDecimal.valueOf(1000.00));
        booking.setBookingDate(LocalDateTime.now().plusDays(3));
        booking = bookingRepository.save(booking);

        BookingStatusUpdateRequest request = new BookingStatusUpdateRequest(BookingStatus.CONFIRMED);

        mockMvc.perform(patch("/api/bookings/" + booking.getId() + "/status")
                        .with(authenticate(operator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    private User persistOperator(String email) {
        User operator = new User();
        operator.setName("Operator " + email);
        operator.setEmail(email);
        operator.setPassword(passwordEncoder.encode("password"));
        operator.setRole(UserRole.OPERATOR);
        return userRepository.save(operator);
    }

    private User persistCustomer(String email) {
        User customer = new User();
        customer.setName("Customer " + email);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode("password"));
        customer.setRole(UserRole.CUSTOMER);
        return userRepository.save(customer);
    }

    private Tour persistTour(User operator, String title, BigDecimal price) {
        Tour tour = new Tour();
        tour.setOperator(operator);
        tour.setTitle(title);
        tour.setDescription(title + " description");
        tour.setPrice(price);
        tour.setLocation("Test Location");
        tour.setMaxCapacity(20);
        tour.setAvailableFrom(LocalDateTime.now().plusDays(7));
        tour.setDurationHours(6);
        return tourRepository.save(tour);
    }

    private RequestPostProcessor authenticate(User user) {
        return user(new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole()));
    }
}
