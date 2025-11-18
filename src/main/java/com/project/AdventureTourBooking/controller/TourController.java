package com.project.AdventureTourBooking.controller;

import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.dto.TourResponse;
import com.project.AdventureTourBooking.security.CustomUserDetails;
import com.project.AdventureTourBooking.service.TourService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
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
        return TourResponse.fromEntity(tourService.getTourById(id));
    }

    @GetMapping("/operators/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR')")
    public List<TourResponse> getToursByOperator(@PathVariable Long operatorId, Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        if (!principal.getId().equals(operatorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operators can only view their own tours");
        }
        return tourService.getToursByOperator(operatorId).stream()
                .map(TourResponse::fromEntity)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<TourResponse> createTour(
            @Valid @RequestBody TourRequest request,
            Authentication authentication
    ) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        var created = tourService.createTour(request, principal.getId());
        TourResponse response = TourResponse.fromEntity(created);
        return ResponseEntity
                .created(URI.create("/api/tours/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public TourResponse updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourRequest request,
            Authentication authentication
    ) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return TourResponse.fromEntity(tourService.updateTour(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTour(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        tourService.deleteTour(id, principal.getId());
    }
}
