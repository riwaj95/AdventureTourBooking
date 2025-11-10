package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.Tour;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TourResponse(
        Long id,
        Long operatorId,
        String operatorName,
        String title,
        String description,
        BigDecimal price,
        String location,
        Integer maxCapacity,
        LocalDateTime availableFrom,
        Integer durationHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TourResponse fromEntity(Tour tour) {
        return new TourResponse(
                tour.getId(),
                tour.getOperator().getId(),
                tour.getOperator().getName(),
                tour.getTitle(),
                tour.getDescription(),
                tour.getPrice(),
                tour.getLocation(),
                tour.getMaxCapacity(),
                tour.getAvailableFrom(),
                tour.getDurationHours(),
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}
