package com.wastewatch.service;

import com.wastewatch.dto.volunteer.VolunteerSummaryDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.Inspection;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.InspectionRepository;
import com.wastewatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService {

    private final UserRepository userRepository;
    private final InspectionRepository inspectionRepository;

    public VolunteerService(UserRepository userRepository, InspectionRepository inspectionRepository) {
        this.userRepository = userRepository;
        this.inspectionRepository = inspectionRepository;
    }

    @Transactional(readOnly = true)
    public List<VolunteerSummaryDto> getAllVolunteers() {
        List<AppUser> volunteers = userRepository.findByRole(UserRole.VOLUNTEER);
        List<VolunteerSummaryDto> result = new ArrayList<>();

        for (AppUser volunteer : volunteers) {
            result.add(buildVolunteerSummary(volunteer));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public VolunteerSummaryDto getVolunteerPerformance(Long volunteerId) {
        AppUser volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", volunteerId));
        return buildVolunteerSummary(volunteer);
    }

    private VolunteerSummaryDto buildVolunteerSummary(AppUser user) {
        VolunteerSummaryDto dto = new VolunteerSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus().name());

        List<Inspection> inspections = inspectionRepository.findByVolunteerIdOrderByInspectionDateDesc(user.getId());
        dto.setInspectionsCount(inspections.size());

        if (!inspections.isEmpty()) {
            double avg = inspections.stream().mapToInt(Inspection::getTotalScore).average().orElse(0.0);
            dto.setAverageScoreGiven(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
            dto.setLastInspectionDate(inspections.get(0).getInspectionDate());
        } else {
            dto.setAverageScoreGiven(0.0);
            dto.setLastInspectionDate(null);
        }

        return dto;
    }
}
