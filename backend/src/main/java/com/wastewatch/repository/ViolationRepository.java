package com.wastewatch.repository;

import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ViolationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, Long> {
    Optional<Violation> findByInspectionId(Long inspectionId);
    List<Violation> findByRoomId(Long roomId);
    List<Violation> findByStatus(ViolationStatus status);
    List<Violation> findAllByOrderByCreatedAtDesc();
    List<Violation> findByStatusOrderByCreatedAtDesc(ViolationStatus status);
    long countByStatus(ViolationStatus status);
    long countByStatusIn(List<ViolationStatus> statuses);
    List<Violation> findByRoomBuildingId(Long buildingId);

    // Analytics Aggregations
    @Query("SELECT COUNT(v) FROM Violation v WHERE v.room.building.area.id = :areaId")
    Long countByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT COUNT(v) FROM Violation v WHERE v.room.building.area.id = :areaId AND v.status = :status")
    Long countByAreaIdAndStatus(@Param("areaId") Long areaId, @Param("status") ViolationStatus status);

    @Query("SELECT COUNT(v) FROM Violation v WHERE v.room.building.id = :buildingId AND v.status != :status")
    Long countByBuildingIdAndStatusNot(@Param("buildingId") Long buildingId, @Param("status") ViolationStatus status);

    @Query("SELECT COUNT(v) FROM Violation v WHERE v.room.building.id = :buildingId AND v.status = :status")
    Long countByBuildingIdAndStatus(@Param("buildingId") Long buildingId, @Param("status") ViolationStatus status);

    long countByRoomId(Long roomId);

    long countByRoomIdAndStatusNot(Long roomId, ViolationStatus status);
}
