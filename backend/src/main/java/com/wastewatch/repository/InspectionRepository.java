package com.wastewatch.repository;

import com.wastewatch.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findAllByOrderByInspectionDateDesc();
    List<Inspection> findByRoomIdOrderByInspectionDateDesc(Long roomId);
    List<Inspection> findByVolunteerIdOrderByInspectionDateDesc(Long volunteerId);
    List<Inspection> findByRoomBuildingIdOrderByInspectionDateDesc(Long buildingId);
    Optional<Inspection> findFirstByRoomIdOrderByInspectionDateDesc(Long roomId);
    Optional<Inspection> findFirstByRoomBuildingIdOrderByInspectionDateDesc(Long buildingId);
    Optional<Inspection> findFirstByRoomIdAndVolunteerIdOrderByCreatedAtDesc(Long roomId, Long volunteerId);
    List<Inspection> findTop20ByOrderByInspectionDateDesc();
    List<Inspection> findTop20ByVolunteerIdOrderByInspectionDateDesc(Long volunteerId);

    // Analytics Aggregations
    @Query("SELECT COUNT(DISTINCT i.room.id) FROM Inspection i")
    Long countDistinctInspectedRooms();

    @Query("SELECT COUNT(DISTINCT i.room.id) FROM Inspection i WHERE i.room.building.area.id = :areaId")
    Long countDistinctInspectedRoomsByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT COUNT(DISTINCT i.room.id) FROM Inspection i WHERE i.room.building.id = :buildingId")
    Long countDistinctInspectedRoomsByBuildingId(@Param("buildingId") Long buildingId);

    @Query("SELECT COUNT(DISTINCT i.room.id) FROM Inspection i WHERE i.volunteer.id = :volunteerId")
    Long countDistinctInspectedRoomsByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COUNT(DISTINCT i.room.building.id) FROM Inspection i WHERE i.volunteer.id = :volunteerId")
    Long countDistinctInspectedBuildingsByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT AVG(i.totalScore) FROM Inspection i")
    Double getOverallAverageScore();

    @Query("SELECT AVG(i.totalScore) FROM Inspection i WHERE i.room.building.area.id = :areaId")
    Double getAverageScoreByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT AVG(i.totalScore) FROM Inspection i WHERE i.room.building.id = :buildingId")
    Double getAverageScoreByBuildingId(@Param("buildingId") Long buildingId);

    @Query("SELECT AVG(i.totalScore) FROM Inspection i WHERE i.volunteer.id = :volunteerId")
    Double getAverageScoreByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.room.building.area.id = :areaId")
    Long countByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.room.building.id = :buildingId")
    Long countByBuildingId(@Param("buildingId") Long buildingId);

    long countByVolunteerId(Long volunteerId);

    @Query("SELECT COUNT(i) FROM Inspection i WHERE i.volunteer.id = :volunteerId AND i.totalScore < 40")
    Long countViolationsFlaggedByVolunteerId(@Param("volunteerId") Long volunteerId);

    List<Inspection> findByInspectionDateBetweenOrderByInspectionDateAsc(LocalDateTime start, LocalDateTime end);

    List<Inspection> findByRoomBuildingAreaIdAndInspectionDateBetween(Long areaId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT i.room.id FROM Inspection i WHERE i.totalScore < 40 GROUP BY i.room.id HAVING COUNT(i) >= 1")
    List<Long> findRoomIdsWithFailedInspections();

    /**
     * Finds the N most recent inspections for a room, ordered newest first.
     * Used to detect persistent non-compliance (3 consecutive POOR inspections).
     */
    @Query("SELECT i FROM Inspection i WHERE i.room.id = :roomId ORDER BY i.inspectionDate DESC LIMIT :limit")
    List<Inspection> findTopNByRoomIdOrderByInspectionDateDesc(@Param("roomId") Long roomId, @Param("limit") int limit);
}
