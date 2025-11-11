package com.project.AdventureTourBooking.controller;

import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.dto.BookingResponse;
import com.project.AdventureTourBooking.dto.BookingStatusUpdateRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.security.CustomUserDetails;
import com.project.AdventureTourBooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication
    ) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Booking saved = bookingService.createBooking(request, principal.getId());
        BookingResponse response = BookingResponse.fromEntity(saved);
        return ResponseEntity
                .created(URI.create("/api/bookings/" + response.id()))
                .body(response);
    }

    @GetMapping("/operator")
    @PreAuthorize("hasRole('OPERATOR')")
    public List<BookingResponse> getBookingsForOperator(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return bookingService.getBookingsForOperator(principal.getId()).stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public BookingResponse updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingStatusUpdateRequest request,
            Authentication authentication
    ) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Booking updated = bookingService.updateBookingStatus(bookingId, request.status(), principal.getId());
        return BookingResponse.fromEntity(updated);
    }
}
