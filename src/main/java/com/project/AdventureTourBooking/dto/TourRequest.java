package com.project.AdventureTourBooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TourRequest(
        Long operatorId,

        String title,

        String description,

        BigDecimal price,

        String location,

        Integer maxCapacity,

        LocalDateTime availableFrom,

        Integer durationHour
) {
}
