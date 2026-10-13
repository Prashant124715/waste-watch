package com.wastewatch.service;

import com.wastewatch.dto.building.BuildingRequestDto;
import com.wastewatch.dto.building.BuildingResponseDto;
import com.wastewatch.entity.Area;
import com.wastewatch.entity.Building;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AreaRepository;
import com.wastewatch.repository.BuildingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final AreaRepository areaRepository;

    public BuildingService(BuildingRepository buildingRepository, AreaRepository areaRepository) {
        this.buildingRepository = buildingRepository;
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingResponseDto> getAllBuildings() {
        return buildingRepository.findAll().stream()
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
        return BuildingResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        buildingRepository.delete(building);
    }
}
