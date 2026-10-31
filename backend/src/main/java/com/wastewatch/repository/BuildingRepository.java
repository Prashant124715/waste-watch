package com.wastewatch.repository;

import com.wastewatch.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByAreaId(Long areaId);
    Optional<Building> findByBuildingCode(String buildingCode);
    boolean existsByBuildingCode(String buildingCode);
    boolean existsByBuildingCodeAndIdNot(String buildingCode, Long id);

    List<Building> findByNameContainingIgnoreCaseOrBuildingCodeContainingIgnoreCase(String name, String buildingCode);

    @Query("SELECT b FROM Building b LEFT JOIN b.area a WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.buildingCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(a IS NOT NULL AND (LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.areaCode) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<Building> searchByAllFields(@Param("query") String query);

    long countByAreaId(Long areaId);
}
