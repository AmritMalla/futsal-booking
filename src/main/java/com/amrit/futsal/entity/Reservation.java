package com.amrit.futsal.entity;

import com.amrit.futsal.model.enumconstant.ReservationStatus;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Reservation")
public class Reservation  extends AbstractEntity<Long> {

    private PlayTime playTime;

    private Customer customer;

    private FutsalGround futsalGround;

    private ReservationStatus reservationStatus;

    public PlayTime getPlayTime() {
        return playTime;
    }

    public void setPlayTime(PlayTime playTime) {
        this.playTime = playTime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public FutsalGround getFutsalGround() {
        return futsalGround;
    }

    public void setFutsalGround(FutsalGround futsalGround) {
        this.futsalGround = futsalGround;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
