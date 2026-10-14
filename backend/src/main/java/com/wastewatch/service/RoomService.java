package com.wastewatch.service;

import com.wastewatch.dto.room.RoomDetailDto;
import com.wastewatch.dto.room.RoomRequestDto;
import com.wastewatch.dto.room.RoomResponseDto;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Building;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.Room;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.InvalidRequestException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.BuildingRepository;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final InspectionRepository inspectionRepository;
    private final AuditLogRepository auditLogRepository;
    private final QrCodeService qrCodeService;

    public RoomService(RoomRepository roomRepository,
                       BuildingRepository buildingRepository,
                       InspectionRepository inspectionRepository,
                       AuditLogRepository auditLogRepository,
                       QrCodeService qrCodeService) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
        this.inspectionRepository = inspectionRepository;
        this.auditLogRepository = auditLogRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Startup verification: ensures every existing room in the database
     * has a valid, non-null, unique QR identifier.
     */
    @PostConstruct
    @Transactional
    public void ensureQrCodeIntegrity() {
        try {
            List<Room> allRooms = roomRepository.findAll();
            int updatedCount = 0;

            for (Room room : allRooms) {
                if (room.getQrCodeToken() == null || room.getQrCodeToken().isBlank()) {
                    String baseCode = room.getBuilding() != null
                            ? room.getBuilding().getBuildingCode()
                            : "BLD";

                    String uniqueToken =
                            generateUniqueQrToken(baseCode, room.getRoomNumber());

                    room.setQrCodeToken(uniqueToken);
                    roomRepository.save(room);
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                log.info(
                        "Assigned unique QR tokens to {} legacy rooms lacking QR identifiers",
                        updatedCount
                );
            }
        } catch (Exception e) {
            log.warn("QR code integrity check deferred: {}", e.getMessage());
        }
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Building",
                                "id",
                                dto.getBuildingId()
                        ));

        if (roomRepository.existsByBuildingIdAndRoomNumber(
                dto.getBuildingId(),
                dto.getRoomNumber().trim())) {

            throw new DuplicateResourceException(
                    String.format(
                            "Room '%s' already exists in building '%s'",
                            dto.getRoomNumber().trim(),
                            building.getName()
                    )
            );
        }

        String qrToken = dto.getQrCodeToken();

        if (qrToken == null || qrToken.isBlank()) {
            qrToken = generateUniqueQrToken(
                    building.getBuildingCode(),
                    dto.getRoomNumber().trim()
            );
        } else {
            qrToken = qrToken.trim();

            if (roomRepository.existsByQrCodeToken(qrToken)) {
                throw new DuplicateResourceException(
                        "Room with QR code token '" + qrToken + "' already exists"
                );
            }
        }

        Room room = new Room();
        room.setBuilding(building);
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setQrCodeToken(qrToken);
        room.setOccupantName(
                dto.getOccupantName() != null
                        ? dto.getOccupantName().trim()
                        : null
        );
        room.setContactPhone(
                dto.getContactPhone() != null
                        ? dto.getContactPhone().trim()
                        : null
        );

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            room.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Room saved = roomRepository.save(room);

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "ROOM_CREATED",
                "Room",
                saved.getId().toString(),
                null,
                String.format(
                        "Room '%s' created on floor %d in building '%s'",
                        saved.getRoomNumber(),
                        saved.getFloorNumber(),
                        building.getName()
                )
        );

        auditLogRepository.save(auditLog);

        return RoomResponseDto.fromEntity(saved);
    }

    @Transactional
    public RoomResponseDto updateRoom(Long id, RoomRequestDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room", "id", id));

        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Building",
                                "id",
                                dto.getBuildingId()
                        ));

        if (roomRepository.existsByBuildingIdAndRoomNumberAndIdNot(
                dto.getBuildingId(),
                dto.getRoomNumber().trim(),
                id)) {

            throw new DuplicateResourceException(
                    String.format(
                            "Another room '%s' already exists in building '%s'",
                            dto.getRoomNumber().trim(),
                            building.getName()
                    )
            );
        }

        String qrToken = dto.getQrCodeToken();

        if (qrToken != null && !qrToken.isBlank()) {
            qrToken = qrToken.trim();

            if (roomRepository.existsByQrCodeTokenAndIdNot(qrToken, id)) {
                throw new DuplicateResourceException(
                        "Another room already exists with QR code token '" +
                                qrToken + "'"
                );
            }

            room.setQrCodeToken(qrToken);
        }

        room.setBuilding(building);
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setOccupantName(
                dto.getOccupantName() != null
                        ? dto.getOccupantName().trim()
                        : null
        );
        room.setContactPhone(
                dto.getContactPhone() != null
                        ? dto.getContactPhone().trim()
                        : null
        );

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            room.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Room updated = roomRepository.save(room);

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "ROOM_UPDATED",
                "Room",
                updated.getId().toString(),
                null,
                String.format(
                        "Room '%s' in building '%s' updated",
                        updated.getRoomNumber(),
                        building.getName()
                )
        );

        auditLogRepository.save(auditLog);

        return RoomResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room", "id", id));

        // Record Audit Log (Phase 14 Governance)
        AuditLog auditLog = new AuditLog(
                null,
                "ROOM_DELETED",
                "Room",
                id.toString(),
                null,
                String.format(
                        "Room '%s' (ID: %d) deleted",
                        room.getRoomNumber(),
                        id
                )
        );

        auditLogRepository.save(auditLog);

        roomRepository.delete(room);
    }

    /**
     * Lookup room metadata safely by QR code token.
     */
    @Transactional(readOnly = true)
    public RoomDetailDto getRoomByQrCode(String qrCodeToken) {
        if (qrCodeToken == null || qrCodeToken.trim().isEmpty()) {
            throw new InvalidRequestException(
                    "QR code token cannot be empty"
            );
        }

        String cleanToken = qrCodeToken.trim();

        Room room = roomRepository.findByQrCodeToken(cleanToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found for the provided QR code: " +
                                        cleanToken
                        ));

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
            dto.setBuildingCode(room.getBuilding().getBuildingCode());

            if (room.getBuilding().getArea() != null) {
                dto.setAreaId(room.getBuilding().getArea().getId());
                dto.setAreaName(room.getBuilding().getArea().getName());
                dto.setAreaCode(room.getBuilding().getArea().getAreaCode());
            }
        }

        // Attach Base64 data URL for instant rendering on frontend
        try {
            dto.setQrCodeDataUrl(
                    qrCodeService.generateQrCodeDataUrl(
                            room.getQrCodeToken(),
                            260,
                            260
                    )
            );
        } catch (Exception e) {
            log.warn(
                    "Could not generate inline QR data URL for room {}: {}",
                    room.getId(),
                    e.getMessage()
            );
        }

        List<Inspection> inspections =
                inspectionRepository.findByRoomIdOrderByInspectionDateDesc(
                        room.getId()
                );

        dto.setTotalInspections(inspections.size());

        if (!inspections.isEmpty()) {
            Inspection latest = inspections.get(0);

            dto.setLastInspectedAt(latest.getInspectionDate());
            dto.setLastScore(latest.getTotalScore());
            dto.setLastClassification(latest.getClassification());
        }

        return dto;
    }

    /**
     * Generate raw PNG image bytes for a room's QR code.
     */
    @Transactional(readOnly = true)
    public byte[] getRoomQrCodeImage(Long roomId, int size) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room", "id", roomId));

        String token = room.getQrCodeToken();

        if (token == null || token.isBlank()) {
            token = "QR-" + room.getId();
        }

        int dimension = size > 0 ? size : 300;

        return qrCodeService.generateQrCodePng(
                token,
                dimension,
                dimension
        );
    }

    /**
     * Generate raw PNG image bytes directly for a given QR code token.
     */
    @Transactional(readOnly = true)
    public byte[] getQrCodeImageByToken(String qrToken, int size) {
        if (qrToken == null || qrToken.trim().isEmpty()) {
            throw new InvalidRequestException(
                    "QR code token cannot be empty"
            );
        }

        String cleanToken = qrToken.trim();

        Room room = roomRepository.findByQrCodeToken(cleanToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found for the provided QR code: " +
                                        cleanToken
                        ));

        int dimension = size > 0 ? size : 300;

        return qrCodeService.generateQrCodePng(
                room.getQrCodeToken(),
                dimension,
                dimension
        );
    }

    /**
     * Regenerate a room's QR code identifier.
     */
    @Transactional
    public RoomResponseDto regenerateRoomQrCode(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room", "id", roomId));

        String buildingCode =
                room.getBuilding() != null
                        ? room.getBuilding().getBuildingCode()
                        : "BLD";

        String newToken =
                generateUniqueQrToken(
                        buildingCode,
                        room.getRoomNumber()
                );

        room.setQrCodeToken(newToken);

        Room updated = roomRepository.save(room);

        log.info(
                "Regenerated QR token for room {} ({}) -> {}",
                room.getId(),
                room.getRoomNumber(),
                newToken
        );

        return RoomResponseDto.fromEntity(updated);
    }

    /**
     * Helper to generate a collision-free unique QR token.
     */
    private String generateUniqueQrToken(
            String buildingCode,
            String roomNumber) {

        String base =
                qrCodeService.formatRoomQrIdentifier(
                        buildingCode,
                        roomNumber
                );

        if (!roomRepository.existsByQrCodeToken(base)) {
            return base;
        }

        for (int i = 0; i < 10; i++) {
            String candidate =
                    base + "-" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 4)
                                    .toUpperCase();

            if (!roomRepository.existsByQrCodeToken(candidate)) {
                return candidate;
            }
        }

        return "QR-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}