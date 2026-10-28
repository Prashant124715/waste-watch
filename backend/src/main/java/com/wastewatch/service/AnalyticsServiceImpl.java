package com.wastewatch.service;

import com.wastewatch.dto.analytics.*;
import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final InspectionRepository inspectionRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final AreaRepository areaRepository;
    private final ViolationRepository violationRepository;
    private final UserRepository userRepository;

    public AnalyticsServiceImpl(InspectionRepository inspectionRepository,
                                RoomRepository roomRepository,
                                BuildingRepository buildingRepository,
                                AreaRepository areaRepository,
                                ViolationRepository violationRepository,
                                UserRepository userRepository) {
        this.inspectionRepository = inspectionRepository;
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
        this.areaRepository = areaRepository;
        this.violationRepository = violationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OverviewAnalyticsDto getOverviewAnalytics() {
        long totalInspections = inspectionRepository.count();
        long totalBuildings = buildingRepository.count();
        Long inspectedBuildingsVal = inspectionRepository.countDistinctInspectedBuildings();
        long inspectedBuildings = inspectedBuildingsVal != null ? inspectedBuildingsVal : 0L;
        long totalAreas = areaRepository.count();

        List<AppUser> volunteers = userRepository.findByRole(UserRole.VOLUNTEER);
        long totalVolunteers = volunteers.size();

        Double avgScoreVal = inspectionRepository.getOverallAverageScore();
        double overallComplianceRate = round(avgScoreVal != null ? avgScoreVal : 0.0);

        double overallInspectionCoverage = totalBuildings > 0
                ? round(((double) inspectedBuildings / totalBuildings) * 100.0)
                : 0.0;

        long openViolations = violationRepository.countByStatusIn(
                List.of(ViolationStatus.OPEN, ViolationStatus.ACKNOWLEDGED, ViolationStatus.ACTION_REQUIRED));
        long resolvedViolations = violationRepository.countByStatus(ViolationStatus.RESOLVED);
        long totalViolations = openViolations + resolvedViolations;

        double overallViolationResolutionRate = totalViolations > 0
                ? round(((double) resolvedViolations / totalViolations) * 100.0)
                : 100.0;

        // Documented Area/System Health Score Formula:
        // 70% segregation compliance + 20% inspection coverage + 10% violation resolution
        double systemHealthScore = round((0.70 * overallComplianceRate)
                + (0.20 * overallInspectionCoverage)
                + (0.10 * overallViolationResolutionRate));

        // Classifications distribution
        List<Inspection> allInspections = inspectionRepository.findAll();
        Map<String, Long> classificationCounts = new LinkedHashMap<>();
        classificationCounts.put("EXCELLENT", 0L);
        classificationCounts.put("GOOD", 0L);
        classificationCounts.put("NEEDS_IMPROVEMENT", 0L);
        classificationCounts.put("POOR", 0L);

        for (Inspection i : allInspections) {
            if (i.getClassification() != null) {
                String name = i.getClassification().name();
                classificationCounts.put(name, classificationCounts.getOrDefault(name, 0L) + 1);
            }
        }

        return new OverviewAnalyticsDto(
                totalInspections,
                totalBuildings,
                inspectedBuildings,
                totalAreas,
                totalBuildings,
                totalVolunteers,
                overallComplianceRate,
                overallInspectionCoverage,
                overallViolationResolutionRate,
                systemHealthScore,
                openViolations,
                resolvedViolations,
                classificationCounts
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaAnalyticsDto> getAreaAnalytics() {
        List<Area> areas = areaRepository.findAll();
        List<AreaAnalyticsDto> result = new ArrayList<>();

        for (Area area : areas) {
            Long areaId = area.getId();
            long totalBuildings = buildingRepository.countByAreaId(areaId);
            Long inspectedBuildingsVal = inspectionRepository.countDistinctInspectedBuildingsByAreaId(areaId);
            long inspectedBuildings = inspectedBuildingsVal != null ? inspectedBuildingsVal : 0L;

            Long totalInspectionsVal = inspectionRepository.countByAreaId(areaId);
            long totalInspections = totalInspectionsVal != null ? totalInspectionsVal : 0L;

            double inspectionCoverage = totalBuildings > 0
                    ? round(((double) inspectedBuildings / totalBuildings) * 100.0)
                    : 0.0;

            Double avgScoreVal = inspectionRepository.getAverageScoreByAreaId(areaId);
            double avgScore = round(avgScoreVal != null ? avgScoreVal : 0.0);

            String status = determineComplianceStatus(totalInspections, avgScore);

            Long totalViolationsVal = violationRepository.countByAreaId(areaId);
            long totalViolations = totalViolationsVal != null ? totalViolationsVal : 0L;

            Long resolvedViolationsVal = violationRepository.countByAreaIdAndStatus(areaId, ViolationStatus.RESOLVED);
            long resolvedViolations = resolvedViolationsVal != null ? resolvedViolationsVal : 0L;
            long openViolations = Math.max(0, totalViolations - resolvedViolations);

            double resolutionRate = totalViolations > 0
                    ? round(((double) resolvedViolations / totalViolations) * 100.0)
                    : 100.0;

            // Documented Area Health Score formula:
            // 70% segregation compliance + 20% inspection coverage + 10% violation resolution
            double healthScore = round((0.70 * avgScore)
                    + (0.20 * inspectionCoverage)
                    + (0.10 * resolutionRate));

            String confidenceRating = inspectionCoverage >= 75.0 ? "HIGH"
                    : inspectionCoverage >= 40.0 ? "MEDIUM" : "LOW";

            long poorBuildingCount = countPoorBuildingsInArea(areaId);

            AreaAnalyticsDto dto = new AreaAnalyticsDto();
            dto.setAreaId(areaId);
            dto.setAreaName(area.getName());
            dto.setAreaCode(area.getAreaCode());
            dto.setDescription(area.getDescription());
            dto.setTotalBuildings(totalBuildings);
            dto.setTotalRooms(totalBuildings);
            dto.setInspectedRooms(inspectedBuildings);
            dto.setTotalInspections(totalInspections);
            dto.setInspectionCoverage(inspectionCoverage);
            dto.setAverageCompliance(avgScore);
            dto.setComplianceStatus(status);
            dto.setPoorRoomCount(poorBuildingCount);
            dto.setTotalViolations(totalViolations);
            dto.setOpenViolations(openViolations);
            dto.setResolvedViolations(resolvedViolations);
            dto.setViolationResolutionRate(resolutionRate);
            dto.setAreaHealthScore(healthScore);
            dto.setConfidenceRating(confidenceRating);

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingAnalyticsDto> getBuildingAnalytics() {
        List<Building> buildings = buildingRepository.findAll();
        List<BuildingAnalyticsDto> result = new ArrayList<>();

        for (Building building : buildings) {
            Long buildingId = building.getId();
            Long totalInspectionsVal = inspectionRepository.countByBuildingId(buildingId);
            long totalInspections = totalInspectionsVal != null ? totalInspectionsVal : 0L;

            double inspectionCoverage = totalInspections > 0 ? 100.0 : 0.0;

            Double avgScoreVal = inspectionRepository.getAverageScoreByBuildingId(buildingId);
            double avgScore = round(avgScoreVal != null ? avgScoreVal : 0.0);

            String status = determineComplianceStatus(totalInspections, avgScore);

            Long openViolationsVal = violationRepository.countByBuildingIdAndStatusNot(buildingId, ViolationStatus.RESOLVED);
            long openViolations = openViolationsVal != null ? openViolationsVal : 0L;

            Long resolvedViolationsVal = violationRepository.countByBuildingIdAndStatus(buildingId, ViolationStatus.RESOLVED);
            long resolvedViolations = resolvedViolationsVal != null ? resolvedViolationsVal : 0L;

            long poorInspectionCount = countPoorInspectionsInBuilding(buildingId);

            BuildingAnalyticsDto dto = new BuildingAnalyticsDto();
            dto.setBuildingId(buildingId);
            dto.setBuildingName(building.getName());
            dto.setBuildingCode(building.getBuildingCode());
            dto.setBuildingType(building.getBuildingType() != null ? building.getBuildingType().name() : null);
            if (building.getArea() != null) {
                dto.setAreaId(building.getArea().getId());
                dto.setAreaName(building.getArea().getName());
            }
            dto.setLatitude(building.getLatitude() != null ? building.getLatitude().doubleValue() : null);
            dto.setLongitude(building.getLongitude() != null ? building.getLongitude().doubleValue() : null);
            dto.setTotalRooms(1L);
            dto.setInspectedRooms(totalInspections > 0 ? 1L : 0L);
            dto.setTotalInspections(totalInspections);
            dto.setInspectionCoverage(inspectionCoverage);
            dto.setAverageScore(avgScore);
            dto.setComplianceStatus(status);
            dto.setPoorRoomCount(poorInspectionCount);
            dto.setOpenViolationsCount(openViolations);
            dto.setResolvedViolationsCount(resolvedViolations);

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public TrendAnalyticsDto getTrendAnalytics(String period, Integer days) {
        String periodType = (period != null && !period.isBlank()) ? period.toLowerCase().trim() : "daily";
        int lookbackDays = (days != null && days > 0) ? days : 30;

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(lookbackDays);

        List<Inspection> inspections = inspectionRepository.findByInspectionDateBetweenOrderByInspectionDateAsc(start, end);

        Map<String, List<Inspection>> buckets = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>();

        DateTimeFormatter dayKeyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dayLabelFmt = DateTimeFormatter.ofPattern("MMM d");
        DateTimeFormatter monthKeyFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter monthLabelFmt = DateTimeFormatter.ofPattern("MMM yyyy");

        if ("monthly".equals(periodType)) {
            YearMonth startYm = YearMonth.from(start);
            YearMonth endYm = YearMonth.from(end);
            YearMonth cur = startYm;
            while (!cur.isAfter(endYm)) {
                String k = cur.format(monthKeyFmt);
                buckets.put(k, new ArrayList<>());
                labels.put(k, cur.format(monthLabelFmt));
                cur = cur.plusMonths(1);
            }
        } else if ("weekly".equals(periodType)) {
            LocalDate cur = start.toLocalDate();
            LocalDate endD = end.toLocalDate();
            while (!cur.isAfter(endD)) {
                int week = cur.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = cur.get(IsoFields.WEEK_BASED_YEAR);
                String k = year + "-W" + String.format("%02d", week);
                if (!buckets.containsKey(k)) {
                    buckets.put(k, new ArrayList<>());
                    labels.put(k, "W" + week + " " + year);
                }
                cur = cur.plusDays(7);
            }
        } else {
            // Default: daily
            LocalDate cur = start.toLocalDate();
            LocalDate endD = end.toLocalDate();
            while (!cur.isAfter(endD)) {
                String k = cur.format(dayKeyFmt);
                buckets.put(k, new ArrayList<>());
                labels.put(k, cur.format(dayLabelFmt));
                cur = cur.plusDays(1);
            }
        }

        // Place inspections into buckets
        for (Inspection i : inspections) {
            if (i.getInspectionDate() == null) continue;
            String key;
            if ("monthly".equals(periodType)) {
                key = i.getInspectionDate().format(monthKeyFmt);
            } else if ("weekly".equals(periodType)) {
                int week = i.getInspectionDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = i.getInspectionDate().get(IsoFields.WEEK_BASED_YEAR);
                key = year + "-W" + String.format("%02d", week);
            } else {
                key = i.getInspectionDate().format(dayKeyFmt);
            }

            if (buckets.containsKey(key)) {
                buckets.get(key).add(i);
            }
        }

        List<TrendPointDto> points = new ArrayList<>();
        for (Map.Entry<String, List<Inspection>> entry : buckets.entrySet()) {
            String k = entry.getKey();
            List<Inspection> list = entry.getValue();
            long count = list.size();
            double avgScore = 0.0;
            long exc = 0, good = 0, ni = 0, poor = 0, viols = 0;

            if (count > 0) {
                double total = 0;
                for (Inspection i : list) {
                    int score = i.getTotalScore() != null ? i.getTotalScore() : 0;
                    total += score;
                    if (score < 40) viols++;

                    ComplianceClassification cls = i.getClassification();
                    if (cls == ComplianceClassification.EXCELLENT) exc++;
                    else if (cls == ComplianceClassification.GOOD) good++;
                    else if (cls == ComplianceClassification.NEEDS_IMPROVEMENT) ni++;
                    else if (cls == ComplianceClassification.POOR) poor++;
                }
                avgScore = round(total / count);
            }

            points.add(new TrendPointDto(
                    k,
                    labels.getOrDefault(k, k),
                    count,
                    avgScore,
                    exc,
                    good,
                    ni,
                    poor,
                    viols
            ));
        }

        return new TrendAnalyticsDto(periodType, lookbackDays, points);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaImprovementDto> getAreaImprovements(Integer periodDays) {
        int window = (periodDays != null && periodDays > 0) ? periodDays : 14;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentStart = now.minusDays(window);
        LocalDateTime previousStart = now.minusDays(window * 2L);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        String recentLabel = recentStart.format(fmt) + " – " + now.format(fmt);
        String previousLabel = previousStart.format(fmt) + " – " + recentStart.format(fmt);

        List<Area> areas = areaRepository.findAll();
        List<AreaImprovementDto> result = new ArrayList<>();

        for (Area area : areas) {
            Long areaId = area.getId();
            long totalBuildings = buildingRepository.countByAreaId(areaId);

            List<Inspection> recentInspections = inspectionRepository
                    .findByEffectiveAreaIdAndInspectionDateBetween(areaId, recentStart, now);
            List<Inspection> previousInspections = inspectionRepository
                    .findByEffectiveAreaIdAndInspectionDateBetween(areaId, previousStart, recentStart);

            long recentCount = recentInspections.size();
            long previousCount = previousInspections.size();

            double recentAvg = recentCount > 0
                    ? round(recentInspections.stream().mapToInt(i -> i.getTotalScore() != null ? i.getTotalScore() : 0).average().orElse(0.0))
                    : 0.0;

            double previousAvg = previousCount > 0
                    ? round(previousInspections.stream().mapToInt(i -> i.getTotalScore() != null ? i.getTotalScore() : 0).average().orElse(0.0))
                    : 0.0;

            double scoreDelta = round(recentAvg - previousAvg);

            long recentDistinctBuildings = recentInspections.stream()
                    .map(i -> i.getBuilding() != null ? i.getBuilding().getId() : (i.getRoom() != null && i.getRoom().getBuilding() != null ? i.getRoom().getBuilding().getId() : null))
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            long previousDistinctBuildings = previousInspections.stream()
                    .map(i -> i.getBuilding() != null ? i.getBuilding().getId() : (i.getRoom() != null && i.getRoom().getBuilding() != null ? i.getRoom().getBuilding().getId() : null))
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            double recentCoverage = totalBuildings > 0
                    ? round(((double) recentDistinctBuildings / totalBuildings) * 100.0)
                    : 0.0;
            double previousCoverage = totalBuildings > 0
                    ? round(((double) previousDistinctBuildings / totalBuildings) * 100.0)
                    : 0.0;
            double coverageDelta = round(recentCoverage - previousCoverage);

            String trendStatus;
            if (recentCount == 0 && previousCount == 0) {
                trendStatus = "INSUFFICIENT_DATA";
            } else if (scoreDelta >= 5.0) {
                trendStatus = "IMPROVED";
            } else if (scoreDelta <= -5.0) {
                trendStatus = "DECLINED";
            } else {
                trendStatus = "STABLE";
            }

            AreaImprovementDto dto = new AreaImprovementDto();
            dto.setAreaId(areaId);
            dto.setAreaName(area.getName());
            dto.setAreaCode(area.getAreaCode());
            dto.setRecentPeriodLabel(recentLabel);
            dto.setPreviousPeriodLabel(previousLabel);
            dto.setRecentAverageScore(recentAvg);
            dto.setPreviousAverageScore(previousAvg);
            dto.setScoreDelta(scoreDelta);
            dto.setRecentCoverage(recentCoverage);
            dto.setPreviousCoverage(previousCoverage);
            dto.setCoverageDelta(coverageDelta);
            dto.setRecentInspectionsCount(recentCount);
            dto.setPreviousInspectionsCount(previousCount);
            dto.setTrendStatus(trendStatus);

            result.add(dto);
        }

        // Sort: Most improved first
        result.sort((a, b) -> Double.compare(b.getScoreDelta(), a.getScoreDelta()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public HotspotAnalyticsDto getHotspots() {
        // 1. Repeat Offender Rooms
        List<RepeatOffenderRoomDto> repeatOffenderRooms = new ArrayList<>();
        List<Room> allRooms = roomRepository.findAll();

        for (Room room : allRooms) {
            List<Inspection> roomInspections = inspectionRepository.findByRoomIdOrderByInspectionDateDesc(room.getId());
            if (roomInspections.isEmpty()) continue;

            long failedCount = roomInspections.stream()
                    .filter(i -> i.getTotalScore() != null && i.getTotalScore() < 40)
                    .count();

            long openViols = violationRepository.countByRoomIdAndStatusNot(room.getId(), ViolationStatus.RESOLVED);

            if (failedCount >= 2 || (failedCount >= 1 && openViols > 0)) {
                Inspection latest = roomInspections.get(0);
                long totalCount = roomInspections.size();
                double failureRate = round(((double) failedCount / totalCount) * 100.0);

                String riskLevel;
                if (failedCount >= 3 || (failedCount >= 2 && openViols > 0)) {
                    riskLevel = "CRITICAL";
                } else if (failedCount >= 2) {
                    riskLevel = "HIGH";
                } else {
                    riskLevel = "MEDIUM";
                }

                RepeatOffenderRoomDto rDto = new RepeatOffenderRoomDto();
                rDto.setRoomId(room.getId());
                rDto.setRoomNumber(room.getRoomNumber());
                rDto.setFloorNumber(room.getFloorNumber());
                if (room.getBuilding() != null) {
                    rDto.setBuildingId(room.getBuilding().getId());
                    rDto.setBuildingName(room.getBuilding().getName());
                    if (room.getBuilding().getArea() != null) {
                        rDto.setAreaId(room.getBuilding().getArea().getId());
                        rDto.setAreaName(room.getBuilding().getArea().getName());
                    }
                }
                rDto.setFailedInspectionsCount(failedCount);
                rDto.setTotalInspectionsCount(totalCount);
                rDto.setFailureRate(failureRate);
                rDto.setLatestScore(latest.getTotalScore());
                rDto.setLatestClassification(latest.getClassification() != null ? latest.getClassification().name() : null);
                rDto.setLastInspectionDate(latest.getInspectionDate());
                rDto.setOpenViolationsCount(openViols);
                rDto.setRiskLevel(riskLevel);

                repeatOffenderRooms.add(rDto);
            }
        }

        // Sort repeat offenders: failed count desc, then latest score asc
        repeatOffenderRooms.sort((a, b) -> {
            int cmp = Long.compare(b.getFailedInspectionsCount(), a.getFailedInspectionsCount());
            if (cmp != 0) return cmp;
            return Integer.compare(
                    a.getLatestScore() != null ? a.getLatestScore() : 0,
                    b.getLatestScore() != null ? b.getLatestScore() : 0);
        });

        // 2. Problem Buildings
        List<ProblemBuildingDto> problemBuildings = new ArrayList<>();
        List<BuildingAnalyticsDto> bAnalytics = getBuildingAnalytics();

        for (BuildingAnalyticsDto b : bAnalytics) {
            double poorPercentage = b.getInspectedRooms() > 0
                    ? round(((double) b.getPoorRoomCount() / b.getInspectedRooms()) * 100.0)
                    : 0.0;

            boolean isProblem = (b.getTotalInspections() > 0 && b.getAverageScore() < 50.0)
                    || (b.getInspectedRooms() > 0 && poorPercentage >= 30.0)
                    || b.getOpenViolationsCount() >= 2;

            if (isProblem) {
                String riskLevel;
                if (b.getAverageScore() < 40.0 || poorPercentage >= 50.0) {
                    riskLevel = "CRITICAL";
                } else if (b.getAverageScore() < 50.0 || poorPercentage >= 35.0) {
                    riskLevel = "HIGH";
                } else {
                    riskLevel = "MEDIUM";
                }

                ProblemBuildingDto pDto = new ProblemBuildingDto();
                pDto.setBuildingId(b.getBuildingId());
                pDto.setBuildingName(b.getBuildingName());
                pDto.setBuildingCode(b.getBuildingCode());
                pDto.setAreaId(b.getAreaId());
                pDto.setAreaName(b.getAreaName());
                pDto.setTotalRooms(b.getTotalRooms());
                pDto.setInspectedRooms(b.getInspectedRooms());
                pDto.setPoorRoomCount(b.getPoorRoomCount());
                pDto.setPoorRoomPercentage(poorPercentage);
                pDto.setAverageScore(b.getAverageScore());
                pDto.setOpenViolationsCount(b.getOpenViolationsCount());
                pDto.setRiskLevel(riskLevel);

                problemBuildings.add(pDto);
            }
        }

        problemBuildings.sort((a, b) -> Double.compare(a.getAverageScore(), b.getAverageScore()));

        // 3. Critical Areas
        List<AreaAnalyticsDto> areaAnalytics = getAreaAnalytics();
        List<AreaAnalyticsDto> criticalAreas = areaAnalytics.stream()
                .filter(a -> a.getAreaHealthScore() < 60.0 || "POOR".equals(a.getComplianceStatus()))
                .sorted(Comparator.comparingDouble(AreaAnalyticsDto::getAreaHealthScore))
                .collect(Collectors.toList());

        return new HotspotAnalyticsDto(repeatOffenderRooms, problemBuildings, criticalAreas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerAnalyticsDto> getVolunteerAnalytics() {
        List<AppUser> volunteers = userRepository.findByRole(UserRole.VOLUNTEER);
        List<VolunteerAnalyticsDto> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (AppUser v : volunteers) {
            Long volunteerId = v.getId();
            long count = inspectionRepository.countByVolunteerId(volunteerId);

            Double avgScoreVal = inspectionRepository.getAverageScoreByVolunteerId(volunteerId);
            double avgScore = round(avgScoreVal != null ? avgScoreVal : 0.0);

            Long distinctRoomsVal = inspectionRepository.countDistinctInspectedRoomsByVolunteerId(volunteerId);
            long distinctRooms = distinctRoomsVal != null ? distinctRoomsVal : 0L;

            Long distinctBuildingsVal = inspectionRepository.countDistinctInspectedBuildingsByVolunteerId(volunteerId);
            long distinctBuildings = distinctBuildingsVal != null ? distinctBuildingsVal : 0L;

            Long violsVal = inspectionRepository.countViolationsFlaggedByVolunteerId(volunteerId);
            long violsFlagged = violsVal != null ? violsVal : 0L;

            List<Inspection> recentList = inspectionRepository.findTop20ByVolunteerIdOrderByInspectionDateDesc(volunteerId);
            LocalDateTime lastInspectionDate = (!recentList.isEmpty()) ? recentList.get(0).getInspectionDate() : null;

            List<Inspection> allVolInspections = inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(volunteerId);
            long activeDays = allVolInspections.stream()
                    .map(i -> i.getInspectionDate() != null ? i.getInspectionDate().toLocalDate() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            String activityStatus;
            if (lastInspectionDate == null) {
                activityStatus = "INACTIVE";
            } else if (lastInspectionDate.isAfter(now.minusDays(7))) {
                activityStatus = "ACTIVE";
            } else if (lastInspectionDate.isAfter(now.minusDays(30))) {
                activityStatus = "RECENT";
            } else {
                activityStatus = "INACTIVE";
            }

            VolunteerAnalyticsDto dto = new VolunteerAnalyticsDto();
            dto.setVolunteerId(volunteerId);
            dto.setFullName(v.getFullName());
            dto.setUsername(v.getUsername());
            dto.setEmail(v.getEmail());
            dto.setInspectionsCount(count);
            dto.setAverageScoreAwarded(avgScore);
            dto.setLastInspectionDate(lastInspectionDate);
            dto.setDistinctRoomsInspected(distinctBuildings);
            dto.setDistinctBuildingsCovered(distinctBuildings);
            dto.setViolationsFlagged(violsFlagged);
            dto.setActivityStatus(activityStatus);
            dto.setActiveDaysCount(activeDays);

            result.add(dto);
        }

        // Sort by inspections count desc, then average score desc
        result.sort((a, b) -> {
            int cmp = Long.compare(b.getInspectionsCount(), a.getInspectionsCount());
            if (cmp != 0) return cmp;
            return Double.compare(b.getAverageScoreAwarded(), a.getAverageScoreAwarded());
        });

        return result;
    }

    // Helper methods
    private long countPoorBuildingsInArea(Long areaId) {
        List<Building> buildings = buildingRepository.findByAreaId(areaId);
        long poorCount = 0;
        for (Building b : buildings) {
            Optional<Inspection> latest = inspectionRepository.findFirstByBuildingIdOrderByInspectionDateDesc(b.getId());
            if (latest.isEmpty()) {
                latest = inspectionRepository.findFirstByRoomBuildingIdOrderByInspectionDateDesc(b.getId());
            }
            if (latest.isPresent() && latest.get().getTotalScore() != null && latest.get().getTotalScore() < 40) {
                poorCount++;
            }
        }
        return poorCount;
    }

    private long countPoorInspectionsInBuilding(Long buildingId) {
        List<Inspection> inspections = inspectionRepository.findAllByEffectiveBuildingIdOrderByInspectionDateDesc(buildingId);
        return inspections.stream().filter(i -> i.getTotalScore() != null && i.getTotalScore() < 40).count();
    }

    private String determineComplianceStatus(long count, double avgScore) {
        if (count == 0) return "NO_DATA";
        if (avgScore >= 80.0) return "EXCELLENT";
        if (avgScore >= 60.0) return "GOOD";
        if (avgScore >= 40.0) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }

    private double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
