package com.amrit.futsal.model;

import com.amrit.futsal.model.enumconstant.FutsalGroundStatus;

public class FutsalGroundDTO{

    private Long id;

    private String length;

    private String width;

    private String courtType;

    private FutsalGroundStatus futsalGroundStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
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
