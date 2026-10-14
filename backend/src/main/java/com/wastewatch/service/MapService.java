package com.wastewatch.service;

import com.wastewatch.dto.map.AreaHotspotDto;
import com.wastewatch.dto.map.BuildingMapMarkerDto;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.AreaRepository;
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
import java.util.stream.Collectors;

@Service
public class MapService {

    private final BuildingRepository buildingRepository;
    private final InspectionRepository inspectionRepository;
    private final RoomRepository roomRepository;
    private final ViolationRepository violationRepository;
    private final AreaRepository areaRepository;

    public MapService(BuildingRepository buildingRepository,
                      InspectionRepository inspectionRepository,
                      RoomRepository roomRepository,
                      ViolationRepository violationRepository,
                      AreaRepository areaRepository) {
        this.buildingRepository = buildingRepository;
        this.inspectionRepository = inspectionRepository;
        this.roomRepository = roomRepository;
        this.violationRepository = violationRepository;
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingMapMarkerDto> getBuildingMapMarkers() {
        return getBuildingMapMarkers(null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<BuildingMapMarkerDto> getBuildingMapMarkers(Long areaId,
                                                            String complianceStatus,
                                                            Long buildingId,
                                                            String inspectionStatus) {
        List<Building> buildings = buildingRepository.findAll();
        List<BuildingMapMarkerDto> markers = new ArrayList<>();

        for (Building building : buildings) {
            // Filter by building ID if provided
            if (buildingId != null && !buildingId.equals(building.getId())) {
                continue;
            }

            // Filter by area ID if provided
            if (areaId != null && (building.getArea() == null || !areaId.equals(building.getArea().getId()))) {
                continue;
            }

            BuildingMapMarkerDto marker = buildMarkerDto(building);

            // Filter by compliance status if provided
            if (complianceStatus != null && !complianceStatus.isBlank() && !complianceStatus.equalsIgnoreCase("ALL")) {
                if (marker.getComplianceStatus() == null || !marker.getComplianceStatus().equalsIgnoreCase(complianceStatus)) {
                    continue;
                }
            }

            // Filter by inspection status if provided
            if (inspectionStatus != null && !inspectionStatus.isBlank() && !inspectionStatus.equalsIgnoreCase("ALL")) {
                if ("INSPECTED".equalsIgnoreCase(inspectionStatus) && marker.getTotalInspections() <= 0) {
                    continue;
                }
                if ("UNINSPECTED".equalsIgnoreCase(inspectionStatus) && marker.getTotalInspections() > 0) {
                    continue;
                }
            }

            markers.add(marker);
        }

        return markers;
    }

    private BuildingMapMarkerDto buildMarkerDto(Building building) {
        Long bId = building.getId();
        BuildingMapMarkerDto marker = new BuildingMapMarkerDto();
        marker.setId(bId);
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

        // Validate coordinates
        boolean validCoords = isValidCoordinate(building.getLatitude(), building.getLongitude());
        marker.setHasValidCoordinates(validCoords);

        long totalRooms = roomRepository.countByBuildingId(bId);
        marker.setTotalRooms((int) totalRooms);

        Long inspectedRoomsVal = inspectionRepository.countDistinctInspectedRoomsByBuildingId(bId);
        int inspectedRooms = inspectedRoomsVal != null ? inspectedRoomsVal.intValue() : 0;
        marker.setInspectedRooms(inspectedRooms);

        double coverage = totalRooms > 0
                ? round(((double) inspectedRooms / totalRooms) * 100.0)
                : 0.0;
        marker.setInspectionCoverage(coverage);

        Long totalInspectionsVal = inspectionRepository.countByBuildingId(bId);
        long totalInspections = totalInspectionsVal != null ? totalInspectionsVal : 0L;
        marker.setTotalInspections(totalInspections);

        Double avgScoreVal = inspectionRepository.getAverageScoreByBuildingId(bId);
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
        Optional<Inspection> latestInsp = inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(bId);
        marker.setLastInspectionDate(latestInsp.map(Inspection::getInspectionDate).orElse(null));

        // Poor rooms count in building (< 40)
        long poorRooms = 0;
        List<Room> rooms = roomRepository.findByBuildingId(bId);
        for (Room r : rooms) {
            Optional<Inspection> rLatest = inspectionRepository.findFirstByRoomIdOrderByInspectionDateDesc(r.getId());
            if (rLatest.isPresent() && rLatest.get().getTotalScore() != null && rLatest.get().getTotalScore() < 40) {
                poorRooms++;
            }
        }
        marker.setPoorRoomCount(poorRooms);

        // Open violations count
        Long openCountVal = violationRepository.countByBuildingIdAndStatusNot(bId, ViolationStatus.RESOLVED);
        marker.setOpenViolations(openCountVal != null ? openCountVal.intValue() : 0);

        return marker;
    }

    @Transactional(readOnly = true)
    public List<AreaHotspotDto> getAreaHotspots() {
        List<Area> areas = areaRepository.findAll();
        List<AreaHotspotDto> hotspots = new ArrayList<>();

        for (Area area : areas) {
            AreaHotspotDto dto = new AreaHotspotDto();
            dto.setAreaId(area.getId());
            dto.setAreaName(area.getName());
            dto.setAreaCode(area.getAreaCode());

            List<BuildingMapMarkerDto> areaMarkers = getBuildingMapMarkers(area.getId(), null, null, null);
            dto.setTotalBuildings(areaMarkers.size());

            List<BuildingMapMarkerDto> mapped = areaMarkers.stream()
                    .filter(BuildingMapMarkerDto::isHasValidCoordinates)
                    .collect(Collectors.toList());
            dto.setMappedBuildings(mapped.size());

            // Center coordinates
            if (!mapped.isEmpty()) {
                double avgLat = mapped.stream().mapToDouble(m -> m.getLatitude().doubleValue()).average().orElse(0.0);
                double avgLng = mapped.stream().mapToDouble(m -> m.getLongitude().doubleValue()).average().orElse(0.0);
                dto.setCenterLatitude(BigDecimal.valueOf(round(avgLat)));
                dto.setCenterLongitude(BigDecimal.valueOf(round(avgLng)));
            }

            int poorRooms = 0;
            int openViolations = 0;
            double scoreSum = 0;
            int scoredCount = 0;
            int totalRooms = 0;
            int inspectedRooms = 0;

            for (BuildingMapMarkerDto b : areaMarkers) {
                poorRooms += (int) b.getPoorRoomCount();
                openViolations += b.getOpenViolations();
                totalRooms += (b.getTotalRooms() != null ? b.getTotalRooms() : 0);
                inspectedRooms += (b.getInspectedRooms() != null ? b.getInspectedRooms() : 0);
                if (b.getComplianceScore() != null) {
                    scoreSum += b.getComplianceScore();
                    scoredCount++;
                }
            }

            dto.setPoorRooms(poorRooms);
            dto.setOpenViolations(openViolations);

            double coverage = totalRooms > 0 ? round(((double) inspectedRooms / totalRooms) * 100.0) : 0.0;
            dto.setInspectionCoverage(coverage);

            if (scoredCount > 0) {
                double avg = round(scoreSum / scoredCount);
                dto.setAverageScore(avg);
                if (avg >= 80.0) {
                    dto.setHealthStatus(ComplianceClassification.EXCELLENT);
                    dto.setComplianceStatus("EXCELLENT");
                } else if (avg >= 60.0) {
                    dto.setHealthStatus(ComplianceClassification.GOOD);
                    dto.setComplianceStatus("GOOD");
                } else if (avg >= 40.0) {
                    dto.setHealthStatus(ComplianceClassification.NEEDS_IMPROVEMENT);
                    dto.setComplianceStatus("NEEDS_IMPROVEMENT");
                } else {
                    dto.setHealthStatus(ComplianceClassification.POOR);
                    dto.setComplianceStatus("POOR");
                }
            } else {
                dto.setAverageScore(null);
                dto.setHealthStatus(null);
                dto.setComplianceStatus("NO_DATA");
            }

            // Hotspot severity
            boolean isHotspot = false;
            String severity = "NONE";
            if (dto.getAverageScore() != null && dto.getAverageScore() < 40.0) {
                isHotspot = true;
                severity = "CRITICAL";
            } else if (openViolations >= 2 || (dto.getAverageScore() != null && dto.getAverageScore() < 60.0)) {
                isHotspot = true;
                severity = "WARNING";
            } else if (poorRooms >= 1 || openViolations >= 1) {
                isHotspot = true;
                severity = "ADVISORY";
            }

            dto.setHotspot(isHotspot);
            dto.setHotspotSeverity(severity);
            hotspots.add(dto);
        }

        return hotspots;
    }

    private boolean isValidCoordinate(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) return false;
        if (lat.compareTo(new BigDecimal("-90.0")) < 0 || lat.compareTo(new BigDecimal("90.0")) > 0) return false;
        if (lng.compareTo(new BigDecimal("-180.0")) < 0 || lng.compareTo(new BigDecimal("180.0")) > 0) return false;
        // Exclude default (0.0, 0.0) null-island
        return lat.compareTo(BigDecimal.ZERO) != 0 || lng.compareTo(BigDecimal.ZERO) != 0;
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
