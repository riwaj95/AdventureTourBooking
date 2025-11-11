package com.project.AdventureTourBooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBookingReturnsCreatedResponse() throws Exception {
        Booking booking = sampleBooking();
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(booking);

        BookingRequest request = new BookingRequest(
                1L,
                2L,
                3,
                BigDecimal.valueOf(450.00),
                LocalDateTime.of(2024, 7, 10, 12, 0),
                BookingStatus.CONFIRMED
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/bookings/" + booking.getId()))
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.numberOfPeople").value(3));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    private Booking sampleBooking() {
        Tour tour = new Tour();
        tour.setId(1L);
        User user = new User();
        user.setId(2L);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setTour(tour);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setNumberOfPeople(3);
        booking.setTotalPrice(BigDecimal.valueOf(450.00));
        booking.setBookingDate(LocalDateTime.of(2024, 7, 10, 12, 0));
        booking.setCreatedAt(LocalDateTime.of(2024, 6, 1, 9, 0));
        return booking;
    }
}
