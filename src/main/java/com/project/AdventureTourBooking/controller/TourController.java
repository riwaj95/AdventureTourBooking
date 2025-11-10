package com.project.AdventureTourBooking.controller;

import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.dto.TourResponse;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
@Validated
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourResponse> getAllTours() {
        return tourService.getAllTours().stream()
                .map(TourResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public TourResponse getTourById(@PathVariable Long id) {
        Tour tour = tourService.getTourById(id);
        return TourResponse.fromEntity(tour);
    }

    @GetMapping("/operators/{operatorId}")
    public List<TourResponse> getToursByOperator(@PathVariable Long operatorId) {
        return tourService.getToursByOperator(operatorId).stream()
                .map(TourResponse::fromEntity)
                .toList();
    }

    @PostMapping
    public ResponseEntity<TourResponse> createTour(@RequestBody TourRequest request) {
        Tour created = tourService.createTour(request);
        TourResponse response = TourResponse.fromEntity(created);
        return ResponseEntity
                .created(URI.create("/api/tours/" + response.id()))
                .body(response);
    }
}
