package com.wastewatch.repository;

import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ViolationSeverity;
import com.wastewatch.entity.enums.ViolationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    List<Violation> findByBuildingId(Long buildingId);

    // Phase 13: Advanced filter queries
    List<Violation> findBySeverityOrderByCreatedAtDesc(ViolationSeverity severity);

    @Query("SELECT v FROM Violation v WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:severity IS NULL OR v.severity = :severity) AND " +
           "(:buildingId IS NULL OR v.building.id = :buildingId OR (v.room IS NOT NULL AND v.room.building.id = :buildingId)) AND " +
           "(:areaId IS NULL OR (v.building IS NOT NULL AND v.building.area.id = :areaId) OR (v.room IS NOT NULL AND v.room.building.area.id = :areaId)) AND " +
           "(:roomId IS NULL OR (v.room IS NOT NULL AND v.room.id = :roomId)) AND " +
           "(:from IS NULL OR v.createdAt >= :from) AND " +
           "(:to IS NULL OR v.createdAt <= :to) " +
           "ORDER BY v.createdAt DESC")
    List<Violation> findWithFilters(
            @Param("status") ViolationStatus status,
            @Param("severity") ViolationSeverity severity,
            @Param("buildingId") Long buildingId,
            @Param("areaId") Long areaId,
            @Param("roomId") Long roomId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Analytics Aggregations
    @Query("SELECT COUNT(v) FROM Violation v WHERE (v.building IS NOT NULL AND v.building.area.id = :areaId) OR (v.room IS NOT NULL AND v.room.building.area.id = :areaId)")
    Long countByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT COUNT(v) FROM Violation v WHERE ((v.building IS NOT NULL AND v.building.area.id = :areaId) OR (v.room IS NOT NULL AND v.room.building.area.id = :areaId)) AND v.status = :status")
    Long countByAreaIdAndStatus(@Param("areaId") Long areaId, @Param("status") ViolationStatus status);

    @Query("SELECT COUNT(v) FROM Violation v WHERE ((v.building IS NOT NULL AND v.building.area.id = :areaId) OR (v.room IS NOT NULL AND v.room.building.area.id = :areaId)) AND v.status != :status")
    Long countByAreaIdAndStatusNot(@Param("areaId") Long areaId, @Param("status") ViolationStatus status);

    @Query("SELECT COUNT(v) FROM Violation v WHERE (v.building.id = :buildingId OR (v.room IS NOT NULL AND v.room.building.id = :buildingId)) AND v.status != :status")
    Long countByBuildingIdAndStatusNot(@Param("buildingId") Long buildingId, @Param("status") ViolationStatus status);

    @Query("SELECT COUNT(v) FROM Violation v WHERE (v.building.id = :buildingId OR (v.room IS NOT NULL AND v.room.building.id = :buildingId)) AND v.status = :status")
    Long countByBuildingIdAndStatus(@Param("buildingId") Long buildingId, @Param("status") ViolationStatus status);

    long countByRoomId(Long roomId);

    long countByRoomIdAndStatusNot(Long roomId, ViolationStatus status);
}
