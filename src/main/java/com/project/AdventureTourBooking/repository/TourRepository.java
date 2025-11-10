package com.project.AdventureTourBooking.repository;

import com.project.AdventureTourBooking.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    @Query("SELECT t FROM Tour t WHERE t.operator.id = :operatorId")
    List<Tour> findByOperatorId(@Param("operatorId") Long operatorId);

    List<Tour> findByLocation(String location);

    @Query("SELECT t FROM Tour t WHERE " +
            "(:location IS NULL OR t.location LIKE %:location%) AND " +
            "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.price <= :maxPrice)")
    List<Tour> searchTours(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}
