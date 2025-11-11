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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            TourRepository tourRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(BookingRequest request, Long userId) {
        User user = getCustomer(userId);

        Tour tour = tourRepository.findById(request.tourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));

        BookingStatus status = request.status() != null ? request.status() : BookingStatus.PENDING;

        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setUser(user);
        booking.setStatus(status);
        booking.setNumberOfPeople(request.numberOfPeople());
        booking.setTotalPrice(request.totalPrice());
        booking.setBookingDate(request.bookingDate());

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForOperator(Long operatorId) {
        User operator = getOperator(operatorId);
        return bookingRepository.findByTourOperatorId(operator.getId());
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus status, Long operatorId) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be provided");
        }

        User operator = getOperator(operatorId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getTour().getOperator().getId().equals(operator.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify bookings for other operators");
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private User getCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can create bookings");
        }

        return user;
    }

    private User getOperator(Long operatorId) {
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operator not found"));

        if (operator.getRole() != UserRole.OPERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have operator permissions");
        }

        return operator;
    }
}
