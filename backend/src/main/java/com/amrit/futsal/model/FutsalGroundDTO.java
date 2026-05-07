package com.amrit.futsal.model;

import com.amrit.futsal.model.enumconstant.FutsalGroundStatus;

public class FutsalGroundDTO{

    private Long id;

    private Double length;

    private Double width;

    private String courtType;

    private FutsalGroundStatus futsalGroundStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
