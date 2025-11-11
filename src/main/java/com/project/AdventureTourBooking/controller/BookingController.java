package com.project.AdventureTourBooking.controller;


import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.dto.BookingResponse;
import com.project.AdventureTourBooking.model.Booking;

import com.project.AdventureTourBooking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Validated @RequestBody BookingRequest request) {
        Booking saved = bookingService.createBooking(request);
        BookingResponse response = BookingResponse.fromEntity(saved);
        return ResponseEntity
                .created(URI.create("/api/bookings/" + response.id()))
                .body(response);
    }
}
