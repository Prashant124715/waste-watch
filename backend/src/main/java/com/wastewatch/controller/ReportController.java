package com.wastewatch.controller;

import com.wastewatch.dto.report.*;
import com.wastewatch.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/overview
     * High-level executive compliance report over an optional date range.
     */
    @GetMapping("/overview")
    public ResponseEntity<OverviewComplianceReportDto> getOverviewReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getOverviewReport(from, to));
    }

    /**
     * GET /api/reports/areas
     * Area performance report with health scores and breakdown.
     */
    @GetMapping("/areas")
    public ResponseEntity<List<AreaPerformanceReportDto>> getAreaPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getAreaPerformanceReport(from, to));
    }

    /**
     * GET /api/reports/buildings
     * Building compliance performance matrix.
     */
    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingPerformanceReportDto>> getBuildingPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getBuildingPerformanceReport(from, to));
    }

    /**
     * GET /api/reports/coverage
     * Inspection coverage across areas and buildings.
     */
    @GetMapping("/coverage")
    public ResponseEntity<CoverageReportDto> getCoverageReport() {
        return ResponseEntity.ok(reportService.getCoverageReport());
    }

    /**
     * GET /api/reports/violations
     * Violations breakdown by severity, status, and fines.
     */
    @GetMapping("/violations")
    public ResponseEntity<ViolationReportDto> getViolationReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getViolationReport(from, to));
    }

    /**
     * GET /api/reports/volunteers
     * Volunteer audit activity and productivity statistics.
     */
    @GetMapping("/volunteers")
    public ResponseEntity<List<VolunteerActivityReportDto>> getVolunteerActivityReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getVolunteerActivityReport(from, to));
    }

    /**
     * GET /api/reports/export/csv
     * Direct CSV download for overview, areas, buildings, violations, volunteers, or raw inspections.
     */
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(defaultValue = "overview") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        String csv = reportService.generateCsv(type, from, to);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        String filename = String.format("wastewatch-%s-report.csv", type.trim().toLowerCase());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
