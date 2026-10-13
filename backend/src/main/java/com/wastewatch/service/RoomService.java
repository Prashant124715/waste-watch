package com.wastewatch.service;

import com.wastewatch.dto.room.RoomDetailDto;
import com.wastewatch.dto.room.RoomRequestDto;
import com.wastewatch.dto.room.RoomResponseDto;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final InspectionRepository inspectionRepository;

    public RoomService(RoomRepository roomRepository,
                       BuildingRepository buildingRepository,
                       InspectionRepository inspectionRepository) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
        this.inspectionRepository = inspectionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return RoomResponseDto.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDto> getRoomsByBuildingId(Long buildingId) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new ResourceNotFoundException("Building", "id", buildingId);
        }
        return roomRepository.findByBuildingId(buildingId).stream()
                .map(RoomResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponseDto createRoom(RoomRequestDto dto) {
        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", dto.getBuildingId()));

        if (roomRepository.existsByBuildingIdAndRoomNumber(dto.getBuildingId(), dto.getRoomNumber().trim())) {
            throw new DuplicateResourceException(
                    String.format("Room '%s' already exists in building '%s'", dto.getRoomNumber().trim(), building.getName())
            );
        }

        String qrToken = dto.getQrCodeToken();
        if (qrToken == null || qrToken.isBlank()) {
            qrToken = "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            qrToken = qrToken.trim();
            if (roomRepository.existsByQrCodeToken(qrToken)) {
                throw new DuplicateResourceException("Room with QR code token '" + qrToken + "' already exists");
            }
        }

        Room room = new Room();
        room.setBuilding(building);
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setQrCodeToken(qrToken);
        room.setOccupantName(dto.getOccupantName() != null ? dto.getOccupantName().trim() : null);
        room.setContactPhone(dto.getContactPhone() != null ? dto.getContactPhone().trim() : null);
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            room.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Room saved = roomRepository.save(room);
        return RoomResponseDto.fromEntity(saved);
    }

    @Transactional
    public RoomResponseDto updateRoom(Long id, RoomRequestDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", dto.getBuildingId()));

        if (roomRepository.existsByBuildingIdAndRoomNumberAndIdNot(dto.getBuildingId(), dto.getRoomNumber().trim(), id)) {
            throw new DuplicateResourceException(
                    String.format("Another room '%s' already exists in building '%s'", dto.getRoomNumber().trim(), building.getName())
            );
        }

        String qrToken = dto.getQrCodeToken();
        if (qrToken != null && !qrToken.isBlank()) {
            qrToken = qrToken.trim();
            if (roomRepository.existsByQrCodeTokenAndIdNot(qrToken, id)) {
                throw new DuplicateResourceException("Another room already exists with QR code token '" + qrToken + "'");
            }
            room.setQrCodeToken(qrToken);
        }

        room.setBuilding(building);
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setOccupantName(dto.getOccupantName() != null ? dto.getOccupantName().trim() : null);
        room.setContactPhone(dto.getContactPhone() != null ? dto.getContactPhone().trim() : null);
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            room.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Room updated = roomRepository.save(room);
        return RoomResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public RoomDetailDto getRoomByQrCode(String qrCodeToken) {
        Room room = roomRepository.findByQrCodeToken(qrCodeToken.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "qrCodeToken", qrCodeToken));

        RoomDetailDto dto = new RoomDetailDto();
        dto.setId(room.getId());
        dto.setFloorNumber(room.getFloorNumber());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setQrCodeToken(room.getQrCodeToken());
        dto.setOccupantName(room.getOccupantName());
        dto.setContactPhone(room.getContactPhone());
        dto.setStatus(room.getStatus());

        if (room.getBuilding() != null) {
            dto.setBuildingId(room.getBuilding().getId());
            dto.setBuildingName(room.getBuilding().getName());
            if (room.getBuilding().getArea() != null) {
                dto.setAreaId(room.getBuilding().getArea().getId());
                dto.setAreaName(room.getBuilding().getArea().getName());
            }
        }

        List<Inspection> inspections = inspectionRepository.findByRoomIdOrderByInspectionDateDesc(room.getId());
        dto.setTotalInspections(inspections.size());

        if (!inspections.isEmpty()) {
            Inspection latest = inspections.get(0);
            dto.setLastInspectedAt(latest.getInspectionDate());
            dto.setLastScore(latest.getTotalScore());
            dto.setLastClassification(latest.getClassification());
        }

        return dto;
    }
}
