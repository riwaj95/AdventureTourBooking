package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.TourRequest;
import com.project.AdventureTourBooking.model.Tour;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.TourRepository;
import com.project.AdventureTourBooking.repository.UserRepository;
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
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TourService tourService;

    @Test
    void getAllTours_returnsToursFromRepository() {
        Tour first = new Tour();
        first.setId(1L);
        Tour second = new Tour();
        second.setId(2L);

        when(tourRepository.findAll()).thenReturn(List.of(first, second));

        List<Tour> tours = tourService.getAllTours();

        assertThat(tours).containsExactly(first, second);
    }

    @Test
    void createTour_withValidOperator_savesTour() {
        User operator = createOperator(5L);
        TourRequest request = new TourRequest(
                "Kayaking Adventure",
                "River kayaking",
                BigDecimal.valueOf(199.99),
                "Colorado",
                10,
                LocalDateTime.now(),
                6
        );

        when(userRepository.findById(operator.getId())).thenReturn(Optional.of(operator));
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour tour = invocation.getArgument(0);
            tour.setId(12L);
            return tour;
        });

        Tour created = tourService.createTour(request, operator.getId());

        assertEquals(operator, created.getOperator());
        assertEquals(request.title(), created.getTitle());
        assertEquals(request.price(), created.getPrice());

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());
        Tour persisted = captor.getValue();
        assertEquals(operator, persisted.getOperator());
        assertEquals(request.durationHour(), persisted.getDurationHours());
    }

    @Test
    void getTourById_whenMissing_throwsNotFound() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.getTourById(99L)
        );

        assertEquals("404 NOT_FOUND \"Tour not found\"", exception.getMessage());
    }

    @Test
    void getToursByOperator_whenUserIsNotOperator_throwsForbidden() {
        User customer = new User();
        customer.setId(55L);
        customer.setRole(UserRole.CUSTOMER);

        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.getToursByOperator(customer.getId())
        );

        assertEquals("403 FORBIDDEN \"User does not have operator permissions\"", exception.getMessage());
        verifyNoInteractions(tourRepository);
    }

    private User createOperator(Long id) {
        User operator = new User();
        operator.setId(id);
        operator.setRole(UserRole.OPERATOR);
        return operator;
    }
}
