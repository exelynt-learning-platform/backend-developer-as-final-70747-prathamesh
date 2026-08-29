package com.task.dto;

import com.task.enums.ReservationStatus;
import java.time.LocalDateTime;

public class ReservationUpdateRequest {

    private ReservationStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ReservationUpdateRequest() {
    }

    public ReservationUpdateRequest(ReservationStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
