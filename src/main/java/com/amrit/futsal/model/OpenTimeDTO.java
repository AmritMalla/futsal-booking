package com.amrit.futsal.model;

import com.amrit.futsal.model.enumconstant.Day;

public class OpenTimeDTO {

    private Long id;

    private Day day;

    private String openTime;

    private String closeTime;

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

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }
}
