package com.amrit.futsal.entity;

import com.amrit.futsal.model.enumconstant.Day;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "OpenTime")
public class OpenTime extends AbstractEntity{

    private static final long serialVersionUID = -4246526393439183183L;

    @Column
    private Day day;

    @Column
    private String openTime;

    @Column
    private String closeTime;

    @ManyToOne
    private FutsalGround futsalGround;

    public FutsalGround getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(FutsalGround futsalGround) {
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
