package com.amrit.futsal.model;

import java.time.LocalDateTime;

public class OpenTimeDTO {

    private Long id;

    private LocalDateTime localDate;

    private String startTime;

    private String endTime;

    private Long futsalGround;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(Long futsalGround) {
        this.futsalGround = futsalGround;
    }

    public LocalDateTime getLocalDateTime() {
        return localDate;
    }

    public void setLocalDateTime(LocalDateTime localDate) {
        this.localDate = localDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
