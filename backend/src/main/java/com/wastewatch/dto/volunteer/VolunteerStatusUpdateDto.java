package com.wastewatch.dto.volunteer;

import com.wastewatch.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public class VolunteerStatusUpdateDto {

    @NotNull(message = "Status is required")
    private UserStatus status;

    public VolunteerStatusUpdateDto() {
    }

    public VolunteerStatusUpdateDto(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
