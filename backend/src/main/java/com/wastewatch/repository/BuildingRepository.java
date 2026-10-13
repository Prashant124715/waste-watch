package com.wastewatch.repository;

import com.wastewatch.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByAreaId(Long areaId);
    Optional<Building> findByBuildingCode(String buildingCode);
    boolean existsByBuildingCode(String buildingCode);
    boolean existsByBuildingCodeAndIdNot(String buildingCode, Long id);

    long countByAreaId(Long areaId);
}
