package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank
        String name,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotNull
        UserRole role
) {
}
