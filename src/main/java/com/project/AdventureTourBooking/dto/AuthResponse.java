package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.UserRole;

public record AuthResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
