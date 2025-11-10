package com.project.AdventureTourBooking.repository;

import com.project.AdventureTourBooking.model.Booking;
import com.project.AdventureTourBooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.tour.id = :tourId")
    List<Booking> findByTourId(@Param("tourId") Long tourId);

    @Query("SELECT b FROM Booking b WHERE b.tour.operator.id = :operatorId")
    List<Booking> findByTourOperatorId(@Param("operatorId") Long operatorId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tour.id = :tourId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByTourId(@Param("tourId") Long tourId);
}
