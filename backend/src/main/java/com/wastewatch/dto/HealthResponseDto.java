package com.wastewatch.dto;

import java.time.LocalDateTime;

public class HealthResponseDto {

    private String status;
    private String application;
    private LocalDateTime timestamp;

    public HealthResponseDto() {
        this.status = "UP";
        this.application = "WasteWatch";
        this.timestamp = LocalDateTime.now();
    }

    public HealthResponseDto(String status, String application) {
        this.status = status;
        this.application = application;
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
