package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.BookingStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull
        Long tourId,

        @NotNull
        Integer numberOfPeople,

        @NotNull
        BigDecimal totalPrice,

        @NotNull
        LocalDateTime bookingDate,

        BookingStatus status
) {
}
