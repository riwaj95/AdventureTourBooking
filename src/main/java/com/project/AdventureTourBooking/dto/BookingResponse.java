package com.project.AdventureTourBooking.dto;

import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        BookingStatus status,
        Integer numberOfPeople,
        BigDecimal totalPrice,
        LocalDateTime bookingDate,
        LocalDateTime createdAt
) {
    public static BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getNumberOfPeople(),
                booking.getTotalPrice(),
                booking.getBookingDate(),
                booking.getCreatedAt()
        );
    }
}
