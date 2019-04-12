package com.amrit.futsal.model;

import com.amrit.futsal.model.enumconstant.ReservationStatus;


public class ReservationDTO {

    private Long id;

    private Long playTime;

    private Long customer;

    private ReservationStatus reservationStatus;

    private Long futsalGround;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Long playTime) {
        this.playTime = playTime;
    }

    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    public Long getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(Long futsalGround) {
        this.futsalGround = futsalGround;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
