package com.wastewatch.service;

import com.wastewatch.dto.building.BuildingRequestDto;
import com.wastewatch.dto.building.BuildingResponseDto;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Building;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.InvalidRequestException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AreaRepository;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final AreaRepository areaRepository;
    private final RoomRepository roomRepository;
    private final InspectionRepository inspectionRepository;
    private final AuditLogRepository auditLogRepository;

    public BuildingService(BuildingRepository buildingRepository,
                           AreaRepository areaRepository,
                           RoomRepository roomRepository,
                           InspectionRepository inspectionRepository,
                           AuditLogRepository auditLogRepository) {
        this.buildingRepository = buildingRepository;
        this.areaRepository = areaRepository;
        this.roomRepository = roomRepository;
        this.inspectionRepository = inspectionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingResponseDto> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(BuildingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BuildingResponseDto> searchBuildings(String query) {
        if (query == null || query.trim().isBlank()) {
            return getAllBuildings();
        }
        String q = query.trim();
        return buildingRepository.findByNameContainingIgnoreCaseOrBuildingCodeContainingIgnoreCase(q, q).stream()
                .map(BuildingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuildingResponseDto getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        return BuildingResponseDto.fromEntity(building);
    }

    @Transactional(readOnly = true)
    public List<BuildingResponseDto> getBuildingsByAreaId(Long areaId) {
        if (!areaRepository.existsById(areaId)) {
            throw new ResourceNotFoundException("Area", "id", areaId);
        }
        return buildingRepository.findByAreaId(areaId).stream()
                .map(BuildingResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuildingResponseDto createBuilding(BuildingRequestDto dto) {
        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", dto.getAreaId()));

        if (buildingRepository.existsByBuildingCode(dto.getBuildingCode())) {
            throw new DuplicateResourceException("Building already exists with code: " + dto.getBuildingCode());
        }

        Building building = new Building();
        building.setArea(area);
        building.setBuildingCode(dto.getBuildingCode().trim().toUpperCase());
        building.setName(dto.getName().trim());
        building.setAddress(dto.getAddress().trim());
        building.setLatitude(dto.getLatitude());
        building.setLongitude(dto.getLongitude());
        if (dto.getBuildingType() != null) {
            building.setBuildingType(dto.getBuildingType());
        }
        building.setTotalFloors(dto.getTotalFloors());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            building.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Building saved = buildingRepository.save(building);

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "BUILDING_CREATED",
                "Building",
                saved.getId().toString(),
                null,
                String.format("Building '%s' (%s) created in area '%s'", saved.getName(), saved.getBuildingCode(), area.getName())
        );
        auditLogRepository.save(auditLog);

        return BuildingResponseDto.fromEntity(saved);
    }

    @Transactional
    public BuildingResponseDto updateBuilding(Long id, BuildingRequestDto dto) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", dto.getAreaId()));

        if (buildingRepository.existsByBuildingCodeAndIdNot(dto.getBuildingCode(), id)) {
            throw new DuplicateResourceException("Another building already exists with code: " + dto.getBuildingCode());
        }

        building.setArea(area);
        building.setBuildingCode(dto.getBuildingCode().trim().toUpperCase());
        building.setName(dto.getName().trim());
        building.setAddress(dto.getAddress().trim());
        building.setLatitude(dto.getLatitude());
        building.setLongitude(dto.getLongitude());
        if (dto.getBuildingType() != null) {
            building.setBuildingType(dto.getBuildingType());
        }
        building.setTotalFloors(dto.getTotalFloors());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            building.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Building updated = buildingRepository.save(building);

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "BUILDING_UPDATED",
                "Building",
                updated.getId().toString(),
                null,
                String.format("Building '%s' (%s) updated", updated.getName(), updated.getBuildingCode())
        );
        auditLogRepository.save(auditLog);

        return BuildingResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        if (roomRepository.countByBuildingId(id) > 0) {
            throw new InvalidRequestException("Cannot delete building: Building has associated rooms");
        }

        if (inspectionRepository.countByBuildingId(id) > 0) {
            throw new InvalidRequestException("Cannot delete building: Building has inspection records");
        }

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "BUILDING_DELETED",
                "Building",
                id.toString(),
                null,
                String.format("Building '%s' (%s) deleted", building.getName(), building.getBuildingCode())
        );
        auditLogRepository.save(auditLog);

        buildingRepository.delete(building);
    }
}
