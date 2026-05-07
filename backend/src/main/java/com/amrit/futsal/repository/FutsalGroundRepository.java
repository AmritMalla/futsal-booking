package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalGround;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FutsalGroundRepository extends JpaRepository<FutsalGround, UUID> {

    List<FutsalGround> findByCompanyId(UUID companyId);

    List<FutsalGround> findByCompanyOwnerId(UUID ownerId);

    Optional<FutsalGround> findByName(String name);

    List<FutsalGround> findBySurfaceType(String surfaceType);

    @Query("SELECT g FROM FutsalGround g WHERE " +
            "(:location IS NULL OR LOWER(g.company.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:surfaceType IS NULL OR g.surfaceType = :surfaceType) AND " +
            "(:minPrice IS NULL OR g.pricePerHour >= :minPrice) AND " +
            "(:maxPrice IS NULL OR g.pricePerHour <= :maxPrice)")
    List<FutsalGround> searchGrounds(
            @Param("location") String location,
            @Param("surfaceType") String surfaceType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT g FROM FutsalGround g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(g.company.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(g.company.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FutsalGround> searchByKeyword(@Param("keyword") String keyword);

    List<FutsalGround> findByPricePerHourBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
