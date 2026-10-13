package com.wastewatch.repository;

import com.wastewatch.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBuildingId(Long buildingId);
    Optional<Room> findByQrCodeToken(String qrCodeToken);
    Optional<Room> findByBuildingIdAndRoomNumber(Long buildingId, String roomNumber);
    boolean existsByBuildingIdAndRoomNumber(Long buildingId, String roomNumber);
    boolean existsByBuildingIdAndRoomNumberAndIdNot(Long buildingId, String roomNumber, Long id);
    boolean existsByQrCodeToken(String qrCodeToken);
    boolean existsByQrCodeTokenAndIdNot(String qrCodeToken, Long id);

    // Analytics Aggregations
    @Query("SELECT COUNT(r) FROM Room r WHERE r.building.area.id = :areaId")
    Long countByAreaId(@Param("areaId") Long areaId);

    long countByBuildingId(Long buildingId);
}
