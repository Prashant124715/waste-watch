package com.wastewatch.service;

import com.wastewatch.dto.report.*;
import com.wastewatch.dto.violation.ViolationResponseDto;
import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.ComplianceClassification;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.ViolationSeverity;
import com.wastewatch.entity.enums.ViolationStatus;
import com.wastewatch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;
    private final AreaRepository areaRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ReportService(InspectionRepository inspectionRepository,
                         ViolationRepository violationRepository,
                         AreaRepository areaRepository,
                         BuildingRepository buildingRepository,
                         RoomRepository roomRepository,
                         UserRepository userRepository) {
        this.inspectionRepository = inspectionRepository;
        this.violationRepository = violationRepository;
        this.areaRepository = areaRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // 1. OVERALL COMPLIANCE REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public OverviewComplianceReportDto getOverviewReport(LocalDateTime from, LocalDateTime to) {
        List<Inspection> inspections = getInspectionsInPeriod(from, to);
        List<Violation> violations = getViolationsInPeriod(from, to);
        List<Room> allRooms = roomRepository.findAll();

        OverviewComplianceReportDto dto = new OverviewComplianceReportDto();
        dto.setFrom(from);
        dto.setTo(to);
        dto.setTotalInspections(inspections.size());

        if (!inspections.isEmpty()) {
            double avg = inspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
            dto.setAverageScore(round(avg));
            dto.setComplianceRate(round(avg));

            long exc = inspections.stream().filter(i -> i.getClassification() == ComplianceClassification.EXCELLENT).count();
            long good = inspections.stream().filter(i -> i.getClassification() == ComplianceClassification.GOOD).count();
            long needs = inspections.stream().filter(i -> i.getClassification() == ComplianceClassification.NEEDS_IMPROVEMENT).count();
            long poor = inspections.stream().filter(i -> i.getClassification() == ComplianceClassification.POOR).count();

            dto.setExcellentCount(exc);
            dto.setGoodCount(good);
            dto.setNeedsImprovementCount(needs);
            dto.setPoorCount(poor);
        } else {
            dto.setAverageScore(0.0);
            dto.setComplianceRate(0.0);
        }

        dto.setTotalRooms(allRooms.size());
        Set<Long> inspectedRoomIds = inspections.stream()
                .filter(i -> i.getRoom() != null)
                .map(i -> i.getRoom().getId())
                .collect(Collectors.toSet());
        dto.setInspectedRooms(inspectedRoomIds.size());
        dto.setCoveragePercent(allRooms.isEmpty() ? 0.0 : round(((double) inspectedRoomIds.size() / allRooms.size()) * 100.0));

        dto.setTotalViolations(violations.size());
        long resolved = violations.stream().filter(v -> v.getStatus() == ViolationStatus.RESOLVED).count();
        long open = violations.size() - resolved;
        dto.setResolvedViolations(resolved);
        dto.setOpenViolations(open);
        dto.setResolutionRate(violations.isEmpty() ? 100.0 : round(((double) resolved / violations.size()) * 100.0));

        Set<Long> activeVols = inspections.stream().map(i -> i.getVolunteer().getId()).collect(Collectors.toSet());
        dto.setActiveVolunteers(activeVols.size());

        return dto;
    }

    // -------------------------------------------------------------------------
    // 2. AREA PERFORMANCE REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AreaPerformanceReportDto> getAreaPerformanceReport(LocalDateTime from, LocalDateTime to) {
        List<Area> areas = areaRepository.findAll();
        List<Inspection> allInspections = getInspectionsInPeriod(from, to);
        List<Violation> allViolations = getViolationsInPeriod(from, to);

        List<AreaPerformanceReportDto> reports = new ArrayList<>();

        for (Area area : areas) {
            AreaPerformanceReportDto dto = new AreaPerformanceReportDto();
            dto.setAreaId(area.getId());
            dto.setAreaCode(area.getAreaCode());
            dto.setAreaName(area.getName());
            dto.setCity(area.getCity());

            List<Building> blds = buildingRepository.findByAreaId(area.getId());
            dto.setTotalBuildings(blds.size());

            List<Room> areaRooms = new ArrayList<>();
            for (Building b : blds) {
                areaRooms.addAll(roomRepository.findByBuildingId(b.getId()));
            }
            dto.setTotalRooms(areaRooms.size());

            // Filter inspections belonging to this area
            Set<Long> areaBldIds = blds.stream().map(Building::getId).collect(Collectors.toSet());
            Set<Long> areaRoomIds = areaRooms.stream().map(Room::getId).collect(Collectors.toSet());
            List<Inspection> areaInspections = allInspections.stream()
                    .filter(i -> {
                        if (i.getRoom() != null && areaRoomIds.contains(i.getRoom().getId())) return true;
                        Building eb = i.getEffectiveBuilding();
                        return eb != null && areaBldIds.contains(eb.getId());
                    })
                    .collect(Collectors.toList());

            dto.setTotalInspections(areaInspections.size());

            Set<Long> inspectedRoomIds = areaInspections.stream()
                    .filter(i -> i.getRoom() != null)
                    .map(i -> i.getRoom().getId())
                    .collect(Collectors.toSet());
            dto.setInspectedRooms(inspectedRoomIds.size());
            dto.setCoveragePercent(areaRooms.isEmpty() ? 0.0 : round(((double) inspectedRoomIds.size() / areaRooms.size()) * 100.0));

            if (!areaInspections.isEmpty()) {
                double avg = areaInspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
                dto.setAverageScore(round(avg));
                dto.setExcellentCount(areaInspections.stream().filter(i -> i.getClassification() == ComplianceClassification.EXCELLENT).count());
                dto.setGoodCount(areaInspections.stream().filter(i -> i.getClassification() == ComplianceClassification.GOOD).count());
                dto.setNeedsImprovementCount(areaInspections.stream().filter(i -> i.getClassification() == ComplianceClassification.NEEDS_IMPROVEMENT).count());
                dto.setPoorCount(areaInspections.stream().filter(i -> i.getClassification() == ComplianceClassification.POOR).count());
            } else {
                dto.setAverageScore(0.0);
            }

            // Violations in this area
            List<Violation> areaViolations = allViolations.stream()
                    .filter(v -> {
                        if (v.getRoom() != null && areaRoomIds.contains(v.getRoom().getId())) return true;
                        if (v.getBuilding() != null && areaBldIds.contains(v.getBuilding().getId())) return true;
                        return false;
                    })
                    .collect(Collectors.toList());
            long resolved = areaViolations.stream().filter(v -> v.getStatus() == ViolationStatus.RESOLVED).count();
            long open = areaViolations.size() - resolved;
            dto.setOpenViolations(open);
            dto.setResolvedViolations(resolved);

            double resRate = areaViolations.isEmpty() ? 100.0 : ((double) resolved / areaViolations.size()) * 100.0;
            // Area Health Score Formula: 70% avg compliance + 20% coverage + 10% resolution
            double health = (0.70 * dto.getAverageScore()) + (0.20 * dto.getCoveragePercent()) + (0.10 * resRate);
            dto.setHealthScore(round(health));

            reports.add(dto);
        }

        return reports;
    }

    // -------------------------------------------------------------------------
    // 3. BUILDING PERFORMANCE REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<BuildingPerformanceReportDto> getBuildingPerformanceReport(LocalDateTime from, LocalDateTime to) {
        List<Building> buildings = buildingRepository.findAll();
        List<Inspection> allInspections = getInspectionsInPeriod(from, to);
        List<Violation> allViolations = getViolationsInPeriod(from, to);

        List<BuildingPerformanceReportDto> reports = new ArrayList<>();

        for (Building b : buildings) {
            BuildingPerformanceReportDto dto = new BuildingPerformanceReportDto();
            dto.setBuildingId(b.getId());
            dto.setBuildingCode(b.getBuildingCode());
            dto.setBuildingName(b.getName());
            dto.setAreaName(b.getArea() != null ? b.getArea().getName() : "Unknown");
            dto.setBuildingType(b.getBuildingType() != null ? b.getBuildingType().name() : "N/A");
            dto.setTotalFloors(b.getTotalFloors() != null ? b.getTotalFloors() : 0);

            List<Room> rooms = roomRepository.findByBuildingId(b.getId());
            dto.setTotalRooms(rooms.size());

            Set<Long> bldRoomIds = rooms.stream().map(Room::getId).collect(Collectors.toSet());
            List<Inspection> bldInspections = allInspections.stream()
                    .filter(i -> {
                        if (i.getRoom() != null && bldRoomIds.contains(i.getRoom().getId())) return true;
                        Building eb = i.getEffectiveBuilding();
                        return eb != null && b.getId().equals(eb.getId());
                    })
                    .collect(Collectors.toList());

            dto.setTotalInspections(bldInspections.size());

            Set<Long> inspectedRoomIds = bldInspections.stream()
                    .filter(i -> i.getRoom() != null)
                    .map(i -> i.getRoom().getId())
                    .collect(Collectors.toSet());
            dto.setInspectedRooms(inspectedRoomIds.size());
            dto.setCoveragePercent(rooms.isEmpty() ? 0.0 : round(((double) inspectedRoomIds.size() / rooms.size()) * 100.0));

            if (!bldInspections.isEmpty()) {
                double avg = bldInspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
                dto.setAverageScore(round(avg));
                if (avg >= 80) dto.setComplianceStatus("EXCELLENT");
                else if (avg >= 60) dto.setComplianceStatus("GOOD");
                else if (avg >= 40) dto.setComplianceStatus("NEEDS_IMPROVEMENT");
                else dto.setComplianceStatus("POOR");
            } else {
                dto.setAverageScore(0.0);
                dto.setComplianceStatus("NO_DATA");
            }

            List<Violation> bldViolations = allViolations.stream()
                    .filter(v -> (v.getRoom() != null && bldRoomIds.contains(v.getRoom().getId())) ||
                                 (v.getBuilding() != null && b.getId().equals(v.getBuilding().getId())))
                    .collect(Collectors.toList());
            long resolved = bldViolations.stream().filter(v -> v.getStatus() == ViolationStatus.RESOLVED).count();
            long open = bldViolations.size() - resolved;
            dto.setOpenViolations(open);
            dto.setResolvedViolations(resolved);

            reports.add(dto);
        }

        return reports;
    }

    // -------------------------------------------------------------------------
    // 4. INSPECTION COVERAGE REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public CoverageReportDto getCoverageReport() {
        List<Room> allRooms = roomRepository.findAll();
        List<Inspection> allInspections = inspectionRepository.findAll();

        Set<Long> inspectedRoomIds = allInspections.stream()
                .filter(i -> i.getRoom() != null)
                .map(i -> i.getRoom().getId())
                .collect(Collectors.toSet());

        CoverageReportDto dto = new CoverageReportDto();
        dto.setTotalRooms(allRooms.size());
        dto.setInspectedRooms(inspectedRoomIds.size());
        dto.setUninspectedRooms(Math.max(0, allRooms.size() - inspectedRoomIds.size()));
        dto.setOverallCoveragePercent(allRooms.isEmpty() ? 0.0 : round(((double) inspectedRoomIds.size() / allRooms.size()) * 100.0));

        // Area coverages
        List<CoverageReportDto.AreaCoverageItem> areaCoverageList = new ArrayList<>();
        for (Area area : areaRepository.findAll()) {
            List<Building> blds = buildingRepository.findByAreaId(area.getId());
            List<Room> areaRooms = new ArrayList<>();
            for (Building b : blds) areaRooms.addAll(roomRepository.findByBuildingId(b.getId()));
            long inspectedInArea = areaRooms.stream().filter(r -> inspectedRoomIds.contains(r.getId())).count();
            double cov = areaRooms.isEmpty() ? 0.0 : round(((double) inspectedInArea / areaRooms.size()) * 100.0);
            areaCoverageList.add(new CoverageReportDto.AreaCoverageItem(area.getId(), area.getName(), areaRooms.size(), inspectedInArea, cov));
        }
        dto.setAreaCoverage(areaCoverageList);

        // Building coverages
        List<CoverageReportDto.BuildingCoverageItem> bldCoverageList = new ArrayList<>();
        for (Building b : buildingRepository.findAll()) {
            List<Room> rooms = roomRepository.findByBuildingId(b.getId());
            long inspectedInBld = rooms.stream().filter(r -> inspectedRoomIds.contains(r.getId())).count();
            double cov = rooms.isEmpty() ? 0.0 : round(((double) inspectedInBld / rooms.size()) * 100.0);
            bldCoverageList.add(new CoverageReportDto.BuildingCoverageItem(b.getId(), b.getName(),
                    b.getArea() != null ? b.getArea().getName() : "Unknown", rooms.size(), inspectedInBld, cov));
        }
        dto.setBuildingCoverage(bldCoverageList);

        return dto;
    }

    // -------------------------------------------------------------------------
    // 5. VIOLATIONS REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public ViolationReportDto getViolationReport(LocalDateTime from, LocalDateTime to) {
        List<Violation> violations = getViolationsInPeriod(from, to);

        ViolationReportDto dto = new ViolationReportDto();
        dto.setTotalViolations(violations.size());

        long open = violations.stream().filter(v -> v.getStatus() == ViolationStatus.OPEN).count();
        long ack = violations.stream().filter(v -> v.getStatus() == ViolationStatus.ACKNOWLEDGED).count();
        long act = violations.stream().filter(v -> v.getStatus() == ViolationStatus.ACTION_REQUIRED).count();
        long res = violations.stream().filter(v -> v.getStatus() == ViolationStatus.RESOLVED).count();

        dto.setOpenCount(open);
        dto.setAcknowledgedCount(ack);
        dto.setActionRequiredCount(act);
        dto.setResolvedCount(res);

        long crit = violations.stream().filter(v -> v.getSeverity() == ViolationSeverity.CRITICAL).count();
        long high = violations.stream().filter(v -> v.getSeverity() == ViolationSeverity.HIGH).count();
        long med = violations.stream().filter(v -> v.getSeverity() == ViolationSeverity.MEDIUM).count();
        long low = violations.stream().filter(v -> v.getSeverity() == ViolationSeverity.LOW).count();

        dto.setCriticalCount(crit);
        dto.setHighCount(high);
        dto.setMediumCount(med);
        dto.setLowCount(low);

        BigDecimal totalFines = violations.stream()
                .map(v -> v.getFineAmount() != null ? v.getFineAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalFinesImposed(totalFines);

        dto.setResolutionRate(violations.isEmpty() ? 100.0 : round(((double) res / violations.size()) * 100.0));
        dto.setViolations(violations.stream().map(ViolationResponseDto::fromEntity).collect(Collectors.toList()));

        return dto;
    }

    // -------------------------------------------------------------------------
    // 6. VOLUNTEER ACTIVITY REPORT
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<VolunteerActivityReportDto> getVolunteerActivityReport(LocalDateTime from, LocalDateTime to) {
        List<AppUser> volunteers = userRepository.findByRole(UserRole.VOLUNTEER);
        List<Inspection> allInspections = getInspectionsInPeriod(from, to);

        List<VolunteerActivityReportDto> reports = new ArrayList<>();

        for (AppUser vol : volunteers) {
            VolunteerActivityReportDto dto = new VolunteerActivityReportDto();
            dto.setVolunteerId(vol.getId());
            dto.setUsername(vol.getUsername());
            dto.setFullName(vol.getFullName());
            dto.setEmail(vol.getEmail());
            dto.setStatus(vol.getStatus() != null ? vol.getStatus().name() : "ACTIVE");

            List<Inspection> volInspections = allInspections.stream()
                    .filter(i -> i.getVolunteer().getId().equals(vol.getId()))
                    .collect(Collectors.toList());

            dto.setInspectionsInPeriod(volInspections.size());

            if (!volInspections.isEmpty()) {
                double avg = volInspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
                dto.setAverageScore(round(avg));
                Set<Long> rooms = volInspections.stream()
                        .filter(i -> i.getRoom() != null)
                        .map(i -> i.getRoom().getId())
                        .collect(Collectors.toSet());
                dto.setUniqueRoomsAudited(rooms.size());
                Set<Long> blds = volInspections.stream()
                        .map(Inspection::getEffectiveBuilding)
                        .filter(Objects::nonNull)
                        .map(Building::getId)
                        .collect(Collectors.toSet());
                dto.setUniqueBuildingsAudited(blds.size());

                long failed = volInspections.stream().filter(i -> i.getTotalScore() < 40).count();
                dto.setViolationsFlagged(failed);

                dto.setFirstInspectionDate(volInspections.get(volInspections.size() - 1).getInspectionDate());
                dto.setLastInspectionDate(volInspections.get(0).getInspectionDate());
            } else {
                dto.setAverageScore(0.0);
                dto.setUniqueRoomsAudited(0);
                dto.setUniqueBuildingsAudited(0);
                dto.setViolationsFlagged(0);
            }

            reports.add(dto);
        }

        return reports;
    }

    // -------------------------------------------------------------------------
    // 7. CSV EXPORT ENGINE
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public String generateCsv(String reportType, LocalDateTime from, LocalDateTime to) {
        String type = reportType != null ? reportType.trim().toLowerCase() : "overview";

        switch (type) {
            case "areas":
                return generateAreaCsv(getAreaPerformanceReport(from, to));
            case "buildings":
                return generateBuildingCsv(getBuildingPerformanceReport(from, to));
            case "violations":
                return generateViolationCsv(getViolationReport(from, to).getViolations());
            case "volunteers":
                return generateVolunteerCsv(getVolunteerActivityReport(from, to));
            case "inspections":
                return generateInspectionCsv(getInspectionsInPeriod(from, to));
            case "overview":
            default:
                return generateOverviewCsv(getOverviewReport(from, to));
        }
    }

    private String generateOverviewCsv(OverviewComplianceReportDto r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Total Inspections,").append(r.getTotalInspections()).append("\n");
        sb.append("Overall Average Score,").append(r.getAverageScore()).append("\n");
        sb.append("Compliance Rate %,").append(r.getComplianceRate()).append("\n");
        sb.append("Excellent Count,").append(r.getExcellentCount()).append("\n");
        sb.append("Good Count,").append(r.getGoodCount()).append("\n");
        sb.append("Needs Improvement Count,").append(r.getNeedsImprovementCount()).append("\n");
        sb.append("Poor Count,").append(r.getPoorCount()).append("\n");
        sb.append("Total Rooms,").append(r.getTotalRooms()).append("\n");
        sb.append("Inspected Rooms,").append(r.getInspectedRooms()).append("\n");
        sb.append("Coverage Percent,").append(r.getCoveragePercent()).append("\n");
        sb.append("Total Violations,").append(r.getTotalViolations()).append("\n");
        sb.append("Open Violations,").append(r.getOpenViolations()).append("\n");
        sb.append("Resolved Violations,").append(r.getResolvedViolations()).append("\n");
        sb.append("Resolution Rate %,").append(r.getResolutionRate()).append("\n");
        sb.append("Active Volunteers,").append(r.getActiveVolunteers()).append("\n");
        return sb.toString();
    }

    private String generateAreaCsv(List<AreaPerformanceReportDto> areas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Area Code,Area Name,City,Buildings,Rooms,Inspected Rooms,Coverage %,Total Inspections,Avg Score,Open Violations,Resolved Violations,Health Score\n");
        for (AreaPerformanceReportDto a : areas) {
            sb.append(escape(a.getAreaCode())).append(",")
              .append(escape(a.getAreaName())).append(",")
              .append(escape(a.getCity())).append(",")
              .append(a.getTotalBuildings()).append(",")
              .append(a.getTotalRooms()).append(",")
              .append(a.getInspectedRooms()).append(",")
              .append(a.getCoveragePercent()).append(",")
              .append(a.getTotalInspections()).append(",")
              .append(a.getAverageScore()).append(",")
              .append(a.getOpenViolations()).append(",")
              .append(a.getResolvedViolations()).append(",")
              .append(a.getHealthScore()).append("\n");
        }
        return sb.toString();
    }

    private String generateBuildingCsv(List<BuildingPerformanceReportDto> blds) {
        StringBuilder sb = new StringBuilder();
        sb.append("Building Code,Building Name,Area,Building Type,Total Floors,Total Rooms,Inspected Rooms,Coverage %,Total Inspections,Avg Score,Compliance Status,Open Violations,Resolved Violations\n");
        for (BuildingPerformanceReportDto b : blds) {
            sb.append(escape(b.getBuildingCode())).append(",")
              .append(escape(b.getBuildingName())).append(",")
              .append(escape(b.getAreaName())).append(",")
              .append(escape(b.getBuildingType())).append(",")
              .append(b.getTotalFloors()).append(",")
              .append(b.getTotalRooms()).append(",")
              .append(b.getInspectedRooms()).append(",")
              .append(b.getCoveragePercent()).append(",")
              .append(b.getTotalInspections()).append(",")
              .append(b.getAverageScore()).append(",")
              .append(escape(b.getComplianceStatus())).append(",")
              .append(b.getOpenViolations()).append(",")
              .append(b.getResolvedViolations()).append("\n");
        }
        return sb.toString();
    }

    private String generateViolationCsv(List<ViolationResponseDto> viols) {
        StringBuilder sb = new StringBuilder();
        sb.append("Violation ID,Room,Building,Area,Severity,Status,Inspection Score,Fine Amount,Created At,Resolved At,Description\n");
        for (ViolationResponseDto v : viols) {
            sb.append(v.getId()).append(",")
              .append(escape(v.getRoomNumber())).append(",")
              .append(escape(v.getBuildingName())).append(",")
              .append(escape(v.getAreaName())).append(",")
              .append(escape(v.getSeverity() != null ? v.getSeverity().name() : "")).append(",")
              .append(escape(v.getStatus() != null ? v.getStatus().name() : "")).append(",")
              .append(v.getInspectionScore() != null ? v.getInspectionScore() : "").append(",")
              .append(v.getFineAmount() != null ? v.getFineAmount() : "0.00").append(",")
              .append(v.getCreatedAt() != null ? v.getCreatedAt().toString() : "").append(",")
              .append(v.getResolvedAt() != null ? v.getResolvedAt().toString() : "").append(",")
              .append(escape(v.getDescription())).append("\n");
        }
        return sb.toString();
    }

    private String generateVolunteerCsv(List<VolunteerActivityReportDto> vols) {
        StringBuilder sb = new StringBuilder();
        sb.append("Volunteer ID,Username,Full Name,Email,Status,Inspections In Period,Avg Score,Unique Rooms,Unique Buildings,Violations Flagged,Last Inspection\n");
        for (VolunteerActivityReportDto v : vols) {
            sb.append(v.getVolunteerId()).append(",")
              .append(escape(v.getUsername())).append(",")
              .append(escape(v.getFullName())).append(",")
              .append(escape(v.getEmail())).append(",")
              .append(escape(v.getStatus())).append(",")
              .append(v.getInspectionsInPeriod()).append(",")
              .append(v.getAverageScore()).append(",")
              .append(v.getUniqueRoomsAudited()).append(",")
              .append(v.getUniqueBuildingsAudited()).append(",")
              .append(v.getViolationsFlagged()).append(",")
              .append(v.getLastInspectionDate() != null ? v.getLastInspectionDate().toString() : "").append("\n");
        }
        return sb.toString();
    }

    private String generateInspectionCsv(List<Inspection> inspections) {
        StringBuilder sb = new StringBuilder();
        sb.append("Inspection ID,Date,Room,Building,Volunteer,Wet Waste,Dry Waste,Sanitary Waste,Correct Bins,Labelled Bins,No Mixed,Score,Classification,Status\n");
        for (Inspection i : inspections) {
            sb.append(i.getId()).append(",")
              .append(i.getInspectionDate() != null ? i.getInspectionDate().toString() : "").append(",")
              .append(escape(i.getRoom() != null ? i.getRoom().getRoomNumber() : "BUILDING")).append(",")
              .append(escape(i.getEffectiveBuilding() != null ? i.getEffectiveBuilding().getName() : "")).append(",")
              .append(escape(i.getVolunteer() != null ? i.getVolunteer().getFullName() : "")).append(",")
              .append(Boolean.TRUE.equals(i.getWetWasteSeparated())).append(",")
              .append(Boolean.TRUE.equals(i.getDryWasteSeparated())).append(",")
              .append(Boolean.TRUE.equals(i.getSanitaryWasteSeparated())).append(",")
              .append(Boolean.TRUE.equals(i.getCorrectBinsUsed())).append(",")
              .append(Boolean.TRUE.equals(i.getBinsLabelled())).append(",")
              .append(Boolean.TRUE.equals(i.getNoMixedWaste())).append(",")
              .append(i.getTotalScore()).append(",")
              .append(escape(i.getClassification() != null ? i.getClassification().name() : "")).append(",")
              .append(escape(i.getStatus() != null ? i.getStatus().name() : "")).append("\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------
    private List<Inspection> getInspectionsInPeriod(LocalDateTime from, LocalDateTime to) {
        List<Inspection> all = inspectionRepository.findAllByOrderByInspectionDateDesc();
        return all.stream()
                .filter(i -> {
                    if (from != null && i.getInspectionDate() != null && i.getInspectionDate().isBefore(from)) return false;
                    if (to != null && i.getInspectionDate() != null && i.getInspectionDate().isAfter(to)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Violation> getViolationsInPeriod(LocalDateTime from, LocalDateTime to) {
        List<Violation> all = violationRepository.findAllByOrderByCreatedAtDesc();
        return all.stream()
                .filter(v -> {
                    if (from != null && v.getCreatedAt() != null && v.getCreatedAt().isBefore(from)) return false;
                    if (to != null && v.getCreatedAt() != null && v.getCreatedAt().isAfter(to)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String escape(String data) {
        if (data == null) return "";
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}
