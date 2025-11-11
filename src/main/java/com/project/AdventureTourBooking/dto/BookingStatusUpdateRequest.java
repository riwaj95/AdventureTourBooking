package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record BookingStatusUpdateRequest(
        @NotNull
        BookingStatus status
) {
}
