package com.wastewatch.service;

import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import com.wastewatch.repository.ViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MapService {

    private final BuildingRepository buildingRepository;
    private final InspectionRepository inspectionRepository;
    private final RoomRepository roomRepository;
    private final ViolationRepository violationRepository;

    public MapService(BuildingRepository buildingRepository,
                      InspectionRepository inspectionRepository,
                      RoomRepository roomRepository,
                      ViolationRepository violationRepository) {
        this.buildingRepository = buildingRepository;
        this.inspectionRepository = inspectionRepository;
        this.roomRepository = roomRepository;
        this.violationRepository = violationRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingMapMarkerDto> getBuildingMapMarkers() {
        List<Building> buildings = buildingRepository.findAll();
        List<BuildingMapMarkerDto> markers = new ArrayList<>();

        for (Building building : buildings) {
            Long buildingId = building.getId();
            BuildingMapMarkerDto marker = new BuildingMapMarkerDto();
            marker.setId(buildingId);
            marker.setName(building.getName());
            marker.setBuildingCode(building.getBuildingCode());
            marker.setAddress(building.getAddress());
            marker.setLatitude(building.getLatitude());
            marker.setLongitude(building.getLongitude());
            marker.setBuildingType(building.getBuildingType());
            marker.setTotalFloors(building.getTotalFloors());

            if (building.getArea() != null) {
                marker.setAreaId(building.getArea().getId());
                marker.setAreaName(building.getArea().getName());
                marker.setAreaCode(building.getArea().getAreaCode());
            }

            // Valid coordinates check
            boolean validCoords = building.getLatitude() != null
                    && building.getLongitude() != null
                    && building.getLatitude().compareTo(new BigDecimal("-90.0")) >= 0
                    && building.getLatitude().compareTo(new BigDecimal("90.0")) <= 0
                    && building.getLongitude().compareTo(new BigDecimal("-180.0")) >= 0
                    && building.getLongitude().compareTo(new BigDecimal("180.0")) <= 0
                    && (building.getLatitude().compareTo(BigDecimal.ZERO) != 0 || building.getLongitude().compareTo(BigDecimal.ZERO) != 0);

            marker.setHasValidCoordinates(validCoords);

            long totalRooms = roomRepository.countByBuildingId(buildingId);
            marker.setTotalRooms((int) totalRooms);

            Long inspectedRoomsVal = inspectionRepository.countDistinctInspectedRoomsByBuildingId(buildingId);
            int inspectedRooms = inspectedRoomsVal != null ? inspectedRoomsVal.intValue() : 0;
            marker.setInspectedRooms(inspectedRooms);

            double coverage = totalRooms > 0
                    ? round(((double) inspectedRooms / totalRooms) * 100.0)
                    : 0.0;
            marker.setInspectionCoverage(coverage);

            Long totalInspectionsVal = inspectionRepository.countByBuildingId(buildingId);
            long totalInspections = totalInspectionsVal != null ? totalInspectionsVal : 0L;
            marker.setTotalInspections(totalInspections);

            Double avgScoreVal = inspectionRepository.getAverageScoreByBuildingId(buildingId);
            if (avgScoreVal != null && totalInspections > 0) {
                double roundedScore = round(avgScoreVal);
                marker.setLatestScore(roundedScore);
                marker.setComplianceScore(roundedScore);

                if (roundedScore >= 80.0) {
                    marker.setHealthStatus(ComplianceClassification.EXCELLENT);
                    marker.setComplianceStatus("EXCELLENT");
                } else if (roundedScore >= 60.0) {
                    marker.setHealthStatus(ComplianceClassification.GOOD);
                    marker.setComplianceStatus("GOOD");
                } else if (roundedScore >= 40.0) {
                    marker.setHealthStatus(ComplianceClassification.NEEDS_IMPROVEMENT);
                    marker.setComplianceStatus("NEEDS_IMPROVEMENT");
                } else {
                    marker.setHealthStatus(ComplianceClassification.POOR);
                    marker.setComplianceStatus("POOR");
                }
            } else {
                marker.setLatestScore(null);
                marker.setComplianceScore(null);
                marker.setHealthStatus(null);
                marker.setComplianceStatus("NO_DATA");
            }

            // Latest inspection date
            Optional<Inspection> latestInsp = inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(buildingId);
            marker.setLastInspectionDate(latestInsp.map(Inspection::getInspectionDate).orElse(null));

            // Poor rooms count in building
            long poorRooms = 0;
            List<Room> rooms = roomRepository.findByBuildingId(buildingId);
            for (Room r : rooms) {
                Optional<Inspection> rLatest = inspectionRepository.findFirstByRoomIdOrderByInspectionDateDesc(r.getId());
                if (rLatest.isPresent() && rLatest.get().getTotalScore() != null && rLatest.get().getTotalScore() < 40) {
                    poorRooms++;
                }
            }
            marker.setPoorRoomCount(poorRooms);

            // Open violations count
            Long openCountVal = violationRepository.countByBuildingIdAndStatusNot(buildingId, ViolationStatus.RESOLVED);
            marker.setOpenViolations(openCountVal != null ? openCountVal.intValue() : 0);

            markers.add(marker);
        }

        return markers;
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
