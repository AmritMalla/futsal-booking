package com.amrit.futsal.model;

import com.amrit.futsal.model.enumconstant.PlayTimeStatus;


public class PlayTimeDTO{

    private Long id;
    
    private String startTime;

    private String EndTime;

    private PlayTimeStatus playTimeStatus;

    private Double price;

    private Long futsalGround;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public PlayTimeStatus getPlayTimeStatus() {
        return playTimeStatus;
    }

    public void setPlayTimeStatus(PlayTimeStatus playTimeStatus) {
        this.playTimeStatus = playTimeStatus;
    }

    public Long getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(Long futsalGround) {
        this.futsalGround = futsalGround;
    }
}
