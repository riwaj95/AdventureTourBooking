package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.repository.BookingRepository;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingPersistsBookingWithDefaults() {
        BookingRequest request = new BookingRequest(
                1L,
                2L,
                3,
                BigDecimal.valueOf(450.00),
                LocalDateTime.of(2024, 7, 10, 12, 0),
                null
        );

        Tour tour = new Tour();
        tour.setId(1L);
        User user = new User();
        user.setId(2L);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(15L);
            return saved;
        });

        Booking booking = bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());

        Booking persisted = captor.getValue();
        assertThat(persisted.getTour()).isEqualTo(tour);
        assertThat(persisted.getUser()).isEqualTo(user);
        assertThat(persisted.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(persisted.getNumberOfPeople()).isEqualTo(3);
        assertThat(persisted.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(450.00));
        assertThat(persisted.getBookingDate()).isEqualTo(LocalDateTime.of(2024, 7, 10, 12, 0));

        assertThat(booking.getId()).isEqualTo(15L);
    }

    @Test
    void createBookingUsesProvidedStatusWhenPresent() {
        BookingRequest request = new BookingRequest(
                5L,
                6L,
                2,
                BigDecimal.valueOf(199.50),
                LocalDateTime.of(2024, 8, 20, 9, 30),
                BookingStatus.CONFIRMED
        );

        Tour tour = new Tour();
        tour.setId(5L);
        User user = new User();
        user.setId(6L);

        when(tourRepository.findById(5L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(request);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void createBookingThrowsWhenTourMissing() {
        when(tourRepository.findById(11L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest(
                11L,
                22L,
                1,
                BigDecimal.TEN,
                LocalDateTime.now(),
                null
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo("Tour not found");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createBookingThrowsWhenUserMissing() {
        Tour tour = new Tour();
        tour.setId(11L);
        when(tourRepository.findById(11L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(22L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest(
                11L,
                22L,
                1,
                BigDecimal.TEN,
                LocalDateTime.now(),
                null
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> bookingService.createBooking(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo("User not found");
        verify(bookingRepository, never()).save(any());
    }
}
