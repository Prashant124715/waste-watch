package com.wastewatch.service;

import com.wastewatch.dto.area.AreaRequestDto;
import com.wastewatch.dto.area.AreaResponseDto;
import com.wastewatch.entity.Area;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public List<AreaResponseDto> getAllAreas() {
        return areaRepository.findAll().stream()
                .map(AreaResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AreaResponseDto getAreaById(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        return AreaResponseDto.fromEntity(area);
    }

    @Transactional
    public AreaResponseDto createArea(AreaRequestDto dto) {
        if (areaRepository.existsByAreaCode(dto.getAreaCode())) {
            throw new DuplicateResourceException("Area already exists with area code: " + dto.getAreaCode());
        }

        Area area = new Area();
        area.setAreaCode(dto.getAreaCode().trim().toUpperCase());
        area.setName(dto.getName().trim());
        area.setCity(dto.getCity().trim());
        area.setPincode(dto.getPincode().trim());
        area.setDescription(dto.getDescription());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            area.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Area saved = areaRepository.save(area);
        return AreaResponseDto.fromEntity(saved);
    }

    @Transactional
    public AreaResponseDto updateArea(Long id, AreaRequestDto dto) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));

        if (areaRepository.existsByAreaCodeAndIdNot(dto.getAreaCode(), id)) {
            throw new DuplicateResourceException("Another area already exists with area code: " + dto.getAreaCode());
        }

        area.setAreaCode(dto.getAreaCode().trim().toUpperCase());
        area.setName(dto.getName().trim());
        area.setCity(dto.getCity().trim());
        area.setPincode(dto.getPincode().trim());
        area.setDescription(dto.getDescription());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            area.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Area updated = areaRepository.save(area);
        return AreaResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "id", id));
        areaRepository.delete(area);
    }
}
