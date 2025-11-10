package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.BookingRequest;
import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.repository.BookingRepository;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Booking createBooking(BookingRequest request) {
        Tour tour = tourRepository.findById(request.tourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

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
}
