package com.project.AdventureTourBooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TourRequest(
        @NotBlank
        String title,

        String description,

        @NotNull
        BigDecimal price,

        @NotBlank
        String location,

        @NotNull
        Integer maxCapacity,

        LocalDateTime availableFrom,

        @NotNull
        Integer durationHour
) {
}
