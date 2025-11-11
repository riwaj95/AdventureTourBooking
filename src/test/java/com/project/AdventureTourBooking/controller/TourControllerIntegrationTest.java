package com.project.AdventureTourBooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.service.TourService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TourControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TourService tourService;

    @Test
    void getAllToursReturnsMappedResponses() throws Exception {
        Tour tour = sampleTour();
        when(tourService.getAllTours()).thenReturn(List.of(tour));

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].operatorId").value(10L))
                .andExpect(jsonPath("$[0].operatorName").value("Operator"))
                .andExpect(jsonPath("$[0].title").value("Mountain Hike"))
                .andExpect(jsonPath("$[0].price").value(299.99));
    }

    @Test
    void getTourByIdReturnsTourResponse() throws Exception {
        Tour tour = sampleTour();
        when(tourService.getTourById(1L)).thenReturn(tour);

        mockMvc.perform(get("/api/tours/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Mountain Hike"))
                .andExpect(jsonPath("$.operatorName").value("Operator"));
    }

    @Test
    void createTourReturnsCreatedResponse() throws Exception {
        Tour tour = sampleTour();
        when(tourService.createTour(any(TourRequest.class))).thenReturn(tour);

        TourRequest request = new TourRequest(
                tour.getOperator().getId(),
                tour.getTitle(),
                tour.getDescription(),
                tour.getPrice(),
                tour.getLocation(),
                tour.getMaxCapacity(),
                tour.getAvailableFrom(),
                tour.getDurationHours()
        );

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tours/" + tour.getId()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Mountain Hike"));

        verify(tourService).createTour(any(TourRequest.class));
    }

    @Test
    void getToursByOperatorReturnsList() throws Exception {
        Tour tour = sampleTour();
        when(tourService.getToursByOperator(10L)).thenReturn(List.of(tour));

        mockMvc.perform(get("/api/tours/operators/{operatorId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operatorId").value(10L))
                .andExpect(jsonPath("$[0].title").value("Mountain Hike"));

        verify(tourService).getToursByOperator(10L);
    }

    private Tour sampleTour() {
        User operator = new User();
        operator.setId(10L);
        operator.setName("Operator");
        operator.setRole(UserRole.OPERATOR);

        Tour tour = new Tour();
        tour.setId(1L);
        tour.setOperator(operator);
        tour.setTitle("Mountain Hike");
        tour.setDescription("Explore the mountains");
        tour.setPrice(BigDecimal.valueOf(299.99));
        tour.setLocation("Alps");
        tour.setMaxCapacity(12);
        tour.setDurationHours(8);
        tour.setAvailableFrom(LocalDateTime.of(2024, 6, 1, 10, 0));
        tour.setCreatedAt(LocalDateTime.of(2024, 5, 1, 9, 0));
        tour.setUpdatedAt(LocalDateTime.of(2024, 5, 2, 9, 0));
        return tour;
    }
}
