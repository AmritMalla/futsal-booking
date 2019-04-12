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
    private Double length;
    @Column
    private Double width;
    @Column
    private String courtType;

    @Column
    private FutsalGroundStatus futsalGroundStatus;

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
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
