package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.BookingRepository;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    private User customer;
    private Tour tour;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setRole(UserRole.CUSTOMER);

        tour = new Tour();
        tour.setId(2L);
        tour.setOperator(createOperator(3L));
    }

    @Test
    void createBooking_withValidRequest_assignsDefaultStatus() {
        BookingRequest request = new BookingRequest(
                tour.getId(),
                4,
                BigDecimal.valueOf(250.00),
                LocalDateTime.now(),
                null
        );

        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(tourRepository.findById(tour.getId())).thenReturn(Optional.of(tour));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0, Booking.class);
            booking.setId(10L);
            return booking;
        });

        Booking booking = bookingService.createBooking(request, customer.getId());

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(customer, booking.getUser());
        assertEquals(tour, booking.getTour());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking persisted = captor.getValue();
        assertEquals(BookingStatus.PENDING, persisted.getStatus());
        assertEquals(request.numberOfPeople(), persisted.getNumberOfPeople());
        assertEquals(request.totalPrice(), persisted.getTotalPrice());
    }

    @Test
    void createBooking_whenUserIsNotCustomer_throwsForbidden() {
        User operator = createOperator(5L);
        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));

        BookingRequest request = new BookingRequest(
                tour.getId(),
                2,
                BigDecimal.TEN,
                LocalDateTime.now(),
                BookingStatus.CONFIRMED
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(request, operator.getId())
        );

        assertEquals("403 FORBIDDEN \"Only customers can create bookings\"", exception.getMessage());
        verifyNoInteractions(tourRepository, bookingRepository);
    }

    @Test
    void getBookingsForOperator_returnsBookingsForOperator() {
        User operator = tour.getOperator();
        Booking booking = new Booking();
        booking.setId(22L);
        booking.setTour(tour);

        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(bookingRepository.findByTourOperatorId(operator.getId())).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getBookingsForOperator(operator.getId());

        assertThat(result).containsExactly(booking);
    }

    @Test
    void updateBookingStatus_whenOperatorDoesNotOwnBooking_throwsForbidden() {
        User operator = tour.getOperator();
        User otherOperator = createOperator(9L);

        Booking booking = new Booking();
        booking.setId(30L);
        Tour otherTour = new Tour();
        otherTour.setOperator(otherOperator);
        booking.setTour(otherTour);

        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.updateBookingStatus(booking.getId(), BookingStatus.CANCELLED, operator.getId())
        );

        assertEquals("403 FORBIDDEN \"Cannot modify bookings for other operators\"", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatus_withValidOperator_updatesBooking() {
        User operator = tour.getOperator();

        Booking booking = new Booking();
        booking.setId(44L);
        booking.setTour(tour);
        booking.setStatus(BookingStatus.PENDING);

        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking updated = bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED, operator.getId());

        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
        verify(bookingRepository).save(booking);
    }

    private User createOperator(Long id) {
        User operator = new User();
        operator.setId(id);
        operator.setRole(UserRole.OPERATOR);
        return operator;
    }
}
