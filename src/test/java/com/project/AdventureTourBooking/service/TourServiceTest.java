package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TourService tourService;

    private User operator;

    @BeforeEach
    void setUp() {
        operator = new User();
        operator.setId(10L);
        operator.setName("Adventure Operator");
        operator.setRole(UserRole.OPERATOR);
    }

    @Test
    void getAllToursReturnsRepositoryResults() {
        Tour tour = new Tour();
        when(tourRepository.findAll()).thenReturn(List.of(tour));

        List<Tour> result = tourService.getAllTours();

        assertThat(result).containsExactly(tour);
        verify(tourRepository).findAll();
    }

    @Test
    void getTourByIdReturnsTourWhenFound() {
        Tour tour = new Tour();
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        Tour result = tourService.getTourById(1L);

        assertThat(result).isEqualTo(tour);
        verify(tourRepository).findById(1L);
    }

    @Test
    void getTourByIdThrowsWhenNotFound() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> tourService.getTourById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo("Tour not found");
    }

    @Test
    void getToursByOperatorReturnsToursForOperator() {
        Tour tour = new Tour();
        tour.setOperator(operator);
        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(tourRepository.findByOperatorId(operator.getId())).thenReturn(List.of(tour));

        List<Tour> result = tourService.getToursByOperator(operator.getId());

        assertThat(result).containsExactly(tour);
        verify(tourRepository).findByOperatorId(operator.getId());
    }

    @Test
    void createTourPersistsTourUsingRequestData() {
        TourRequest request = new TourRequest(
                operator.getId(),
                "Mountain Hike",
                "Explore the mountains",
                BigDecimal.valueOf(299.99),
                "Alps",
                12,
                LocalDateTime.of(2024, 6, 1, 10, 0),
                8
        );

        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        Tour created = tourService.createTour(request);

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());

        Tour persisted = captor.getValue();
        assertThat(persisted.getOperator()).isEqualTo(operator);
        assertThat(persisted.getTitle()).isEqualTo("Mountain Hike");
        assertThat(persisted.getDescription()).isEqualTo("Explore the mountains");
        assertThat(persisted.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(299.99));
        assertThat(persisted.getLocation()).isEqualTo("Alps");
        assertThat(persisted.getMaxCapacity()).isEqualTo(12);
        assertThat(persisted.getDurationHours()).isEqualTo(8);
        assertThat(persisted.getAvailableFrom()).isEqualTo(LocalDateTime.of(2024, 6, 1, 10, 0));

        assertThat(created.getId()).isEqualTo(5L);
    }

    @Test
    void createTourThrowsWhenUserIsNotOperator() {
        User customer = new User();
        customer.setId(20L);
        customer.setRole(UserRole.CUSTOMER);

        TourRequest request = new TourRequest(
                customer.getId(),
                "City Tour",
                "Discover downtown",
                BigDecimal.TEN,
                "City",
                5,
                LocalDateTime.now(),
                2
        );

        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> tourService.createTour(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo("User does not have operator permissions");
        verify(tourRepository, never()).save(any());
    }

    @Test
    void getToursByOperatorThrowsWhenUserMissing() {
        when(userRepository.findById(operator.getId())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> tourService.getToursByOperator(operator.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo("Operator not found");
    }
}
