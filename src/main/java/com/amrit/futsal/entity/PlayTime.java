package com.amrit.futsal.entity;

import com.amrit.futsal.model.enumconstant.PlayTimeStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PlayTime")
public class PlayTime extends AbstractEntity {

    private static final long serialVersionUID = -6095836447745131532L;
    @Column
    private String startTime;

    @Column
    private String EndTime;

    @Column
    private PlayTimeStatus playTimeStatus;

    @Column
    private double price;

    @ManyToOne
    private FutsalGround futsalGround;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
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

    public FutsalGround getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(FutsalGround futsalGround) {
        this.futsalGround = futsalGround;
    }
}
