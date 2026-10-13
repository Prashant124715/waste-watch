package com.wastewatch.repository;

import com.wastewatch.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByAreaCode(String areaCode);
    boolean existsByAreaCode(String areaCode);
    boolean existsByAreaCodeAndIdNot(String areaCode, Long id);
}
