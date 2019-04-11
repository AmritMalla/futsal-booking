package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "OpenTime")
public class OpenTime extends AbstractEntity{

    private static final long serialVersionUID = -4246526393439183183L;

    @Column
    private LocalDateTime localDateTime;

    @Column
    private String startTime;

    @Column
    private String endTime;

    @ManyToOne
    private FutsalGround futsalGround;

    public FutsalGround getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(FutsalGround futsalGround) {
        this.futsalGround = futsalGround;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime day) {
        this.localDateTime = localDateTime;
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
