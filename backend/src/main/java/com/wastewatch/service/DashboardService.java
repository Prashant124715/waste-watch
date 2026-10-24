package com.wastewatch.service;

import com.wastewatch.dto.dashboard.*;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.entity.Violation;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;
    private final AreaRepository areaRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final ComplianceScoringService scoringService;

    public DashboardService(InspectionRepository inspectionRepository,
                            ViolationRepository violationRepository,
                            AreaRepository areaRepository,
                            BuildingRepository buildingRepository,
                            RoomRepository roomRepository,
                            ComplianceScoringService scoringService) {
        this.inspectionRepository = inspectionRepository;
        this.violationRepository = violationRepository;
        this.areaRepository = areaRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.scoringService = scoringService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummaryMetrics() {
        long totalAreas = areaRepository.count();
        long totalBuildings = buildingRepository.count();
        long totalRooms = roomRepository.count();

        List<Inspection> inspections = inspectionRepository.findAll();
        long totalInspections = inspections.size();

        LocalDate today = LocalDate.now();
        long inspectionsToday = inspections.stream()
                .filter(i -> i.getInspectionDate() != null && i.getInspectionDate().toLocalDate().isEqual(today))
                .count();

        double overallComplianceRate = 0.0;
        if (totalInspections > 0) {
            double sumScores = inspections.stream()
                    .mapToInt(Inspection::getTotalScore)
                    .sum();
            overallComplianceRate = BigDecimal.valueOf(sumScores / totalInspections)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Set<Long> areasAudited = new HashSet<>();
        Set<Long> buildingsAudited = new HashSet<>();
        Set<Long> roomsCovered = new HashSet<>();
        Set<Long> activeVolunteers = new HashSet<>();

        Map<String, Long> scoreDist = new LinkedHashMap<>();
        scoreDist.put("excellent", 0L);
        scoreDist.put("good", 0L);
        scoreDist.put("needsImprovement", 0L);
        scoreDist.put("poor", 0L);

        // Track latest inspection per room to identify currently poor rooms
        Map<Long, Inspection> latestRoomInspections = new HashMap<>();

        for (Inspection insp : inspections) {
            if (insp.getRoom() != null) {
                Long rId = insp.getRoom().getId();
                roomsCovered.add(rId);

                if (!latestRoomInspections.containsKey(rId) ||
                        (insp.getInspectionDate() != null &&
                                (latestRoomInspections.get(rId).getInspectionDate() == null ||
                                        insp.getInspectionDate().isAfter(latestRoomInspections.get(rId).getInspectionDate())))) {
                    latestRoomInspections.put(rId, insp);
                }

            }
            Building effBuilding = insp.getEffectiveBuilding();
            if (effBuilding != null) {
                buildingsAudited.add(effBuilding.getId());
                if (effBuilding.getArea() != null) {
                    areasAudited.add(effBuilding.getArea().getId());
                }
            }
            if (insp.getVolunteer() != null) {
                activeVolunteers.add(insp.getVolunteer().getId());
            }

            ComplianceClassification c = insp.getClassification();
            if (c == ComplianceClassification.EXCELLENT) {
                scoreDist.put("excellent", scoreDist.get("excellent") + 1);
            } else if (c == ComplianceClassification.GOOD) {
                scoreDist.put("good", scoreDist.get("good") + 1);
            } else if (c == ComplianceClassification.NEEDS_IMPROVEMENT) {
                scoreDist.put("needsImprovement", scoreDist.get("needsImprovement") + 1);
            } else if (c == ComplianceClassification.POOR) {
                scoreDist.put("poor", scoreDist.get("poor") + 1);
            }
        }

        long poorRoomsCount = latestRoomInspections.values().stream()
                .filter(i -> i.getTotalScore() < 40 || i.getClassification() == ComplianceClassification.POOR)
                .count();

        long openViolations = violationRepository.countByStatusIn(
                List.of(ViolationStatus.OPEN, ViolationStatus.ACKNOWLEDGED, ViolationStatus.ACTION_REQUIRED)
        );

        double coveragePercent = totalRooms > 0
                ? BigDecimal.valueOf((double) roomsCovered.size() / totalRooms * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new DashboardSummaryDto(
                totalAreas,
                totalBuildings,
                totalRooms,
                totalInspections,
                inspectionsToday,
                overallComplianceRate,
                poorRoomsCount,
                openViolations,
                coveragePercent,
                areasAudited.size(),
                buildingsAudited.size(),
                roomsCovered.size(),
                activeVolunteers.size(),
                scoreDist
        );
    }

    @Transactional(readOnly = true)
    public ComplianceDistributionDto getComplianceDistribution() {
        List<Inspection> inspections = inspectionRepository.findAll();
        long total = inspections.size();

        long exc = 0, good = 0, needs = 0, poor = 0;
        double sum = 0;

        for (Inspection i : inspections) {
            sum += i.getTotalScore();
            if (i.getClassification() == ComplianceClassification.EXCELLENT) exc++;
            else if (i.getClassification() == ComplianceClassification.GOOD) good++;
            else if (i.getClassification() == ComplianceClassification.NEEDS_IMPROVEMENT) needs++;
            else if (i.getClassification() == ComplianceClassification.POOR) poor++;
        }

        double avg = total > 0 ? BigDecimal.valueOf(sum / total).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        double excPct = total > 0 ? BigDecimal.valueOf((double) exc / total * 100).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        double goodPct = total > 0 ? BigDecimal.valueOf((double) good / total * 100).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        double needsPct = total > 0 ? BigDecimal.valueOf((double) needs / total * 100).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        double poorPct = total > 0 ? BigDecimal.valueOf((double) poor / total * 100).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;

        return new ComplianceDistributionDto(
                avg, total, exc, good, needs, poor, excPct, goodPct, needsPct, poorPct
        );
    }

    @Transactional(readOnly = true)
    public List<AreaComplianceDto> getAreaComplianceStats() {
        List<Area> areas = areaRepository.findAll();
        List<AreaComplianceDto> result = new ArrayList<>();

        for (Area area : areas) {
            List<Building> buildings = buildingRepository.findByAreaId(area.getId());
            long totalBuildings = buildings.size();
            long totalRooms = 0;
            List<Inspection> areaInspections = new ArrayList<>();

            for (Building b : buildings) {
                List<Room> rooms = roomRepository.findByBuildingId(b.getId());
                totalRooms += rooms.size();
                areaInspections.addAll(inspectionRepository.findAllByEffectiveBuildingIdOrderByInspectionDateDesc(b.getId()));
            }

            double avgScore = 0.0;
            if (!areaInspections.isEmpty()) {
                double sum = areaInspections.stream().mapToInt(Inspection::getTotalScore).sum();
                avgScore = BigDecimal.valueOf(sum / areaInspections.size()).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }

            long openViolations = 0;
            for (Inspection i : areaInspections) {
                Optional<Violation> v = violationRepository.findByInspectionId(i.getId());
                if (v.isPresent() && v.get().getStatus() != ViolationStatus.RESOLVED) {
                    openViolations++;
                }
            }

            ComplianceClassification status = scoringService.classifyScore((int) Math.round(avgScore));

            result.add(new AreaComplianceDto(
                    area.getId(),
                    area.getAreaCode(),
                    area.getName(),
                    area.getCity(),
                    totalBuildings,
                    totalRooms,
                    areaInspections.size(),
                    avgScore,
                    openViolations,
                    status
            ));
        }

        // Sort by average compliance score descending
        result.sort((a, b) -> Double.compare(b.getAverageComplianceScore(), a.getAverageComplianceScore()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<DailyTrendDto> getDailyTrends() {
        List<Inspection> inspections = inspectionRepository.findAllByOrderByInspectionDateDesc();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, List<Inspection>> groupedByDate = inspections.stream()
                .filter(i -> i.getInspectionDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getInspectionDate().format(formatter),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DailyTrendDto> trends = new ArrayList<>();
        groupedByDate.forEach((date, list) -> {
            double avg = list.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
            avg = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
            trends.add(new DailyTrendDto(date, avg, list.size()));
        });

        return trends;
    }

    @Transactional(readOnly = true)
    public List<AttentionRequiredDto> getAttentionRequired() {
        List<Room> allRooms = roomRepository.findAll();
        List<AttentionRequiredDto> attentionList = new ArrayList<>();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        for (Room room : allRooms) {
            Optional<Inspection> latestOpt = inspectionRepository.findFirstByRoomIdOrderByInspectionDateDesc(room.getId());

            if (latestOpt.isPresent()) {
                Inspection latest = latestOpt.get();
                boolean activeViol = false;
                Optional<Violation> v = violationRepository.findByInspectionId(latest.getId());
                if (v.isPresent() && v.get().getStatus() != ViolationStatus.RESOLVED) {
                    activeViol = true;
                }

                if (latest.getTotalScore() < 40 || activeViol) {
                    String reason = activeViol ? "ACTIVE_VIOLATION" : "POOR_COMPLIANCE";
                    attentionList.add(new AttentionRequiredDto(
                            room.getId(),
                            room.getRoomNumber(),
                            room.getFloorNumber(),
                            room.getBuilding().getId(),
                            room.getBuilding().getName(),
                            room.getBuilding().getArea() != null ? room.getBuilding().getArea().getName() : "—",
                            latest.getTotalScore(),
                            latest.getClassification(),
                            latest.getInspectionDate(),
                            activeViol,
                            reason
                    ));
                } else if (latest.getInspectionDate() != null && latest.getInspectionDate().isBefore(thirtyDaysAgo)) {
                    attentionList.add(new AttentionRequiredDto(
                            room.getId(),
                            room.getRoomNumber(),
                            room.getFloorNumber(),
                            room.getBuilding().getId(),
                            room.getBuilding().getName(),
                            room.getBuilding().getArea() != null ? room.getBuilding().getArea().getName() : "—",
                            latest.getTotalScore(),
                            latest.getClassification(),
                            latest.getInspectionDate(),
                            false,
                            "OVERDUE_INSPECTION"
                    ));
                }
            } else {
                // Never inspected
                attentionList.add(new AttentionRequiredDto(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getFloorNumber(),
                        room.getBuilding().getId(),
                        room.getBuilding().getName(),
                        room.getBuilding().getArea() != null ? room.getBuilding().getArea().getName() : "—",
                        null,
                        null,
                        null,
                        false,
                        "NEVER_INSPECTED"
                ));
            }
        }

        // Prioritize active violations, then lowest score, then overdue
        attentionList.sort((a, b) -> {
            if (a.isActiveViolation() != b.isActiveViolation()) {
                return Boolean.compare(b.isActiveViolation(), a.isActiveViolation());
            }
            int scoreA = a.getLatestScore() != null ? a.getLatestScore() : -1;
            int scoreB = b.getLatestScore() != null ? b.getLatestScore() : -1;
            return Integer.compare(scoreA, scoreB);
        });

        return attentionList;
    }
}
