package com.amrit.futsal.entity;

import com.amrit.futsal.model.enumconstant.FutsalGroundStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FutsalGround")
public class FutsalGround extends AbstractEntity {

    private static final long serialVersionUID = 46606914159524904L;

    @Column
    private double length;
    @Column
    private double width;
    @Column
    private String courtType;

    @Column
    private FutsalGroundStatus futsalGroundStatus;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public String getCourtType() {
        return courtType;
    }

    public void setCourtType(String courtType) {
        this.courtType = courtType;
    }

    public FutsalGroundStatus getFutsalGroundStatus() {
        return futsalGroundStatus;
    }

    public void setFutsalGroundStatus(FutsalGroundStatus futsalGroundStatus) {
        this.futsalGroundStatus = futsalGroundStatus;
    }
}
