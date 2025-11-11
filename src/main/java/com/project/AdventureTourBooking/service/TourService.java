package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public TourService(TourRepository tourRepository, UserRepository userRepository) {
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tour getTourById(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));
    }

    @Transactional(readOnly = true)
    public List<Tour> getToursByOperator(Long operatorId) {
        User operator = getOperator(operatorId);
        return tourRepository.findByOperatorId(operator.getId());
    }

    @Transactional
    public Tour createTour(TourRequest request, Long operatorId) {
        User operator = getOperator(operatorId);

        Tour tour = new Tour();
        tour.setOperator(operator);
        tour.setTitle(request.title());
        tour.setDescription(request.description());
        tour.setPrice(request.price());
        tour.setLocation(request.location());
        tour.setMaxCapacity(request.maxCapacity());
        tour.setDurationHours(request.durationHour());
        tour.setAvailableFrom(request.availableFrom());

        return tourRepository.save(tour);
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
