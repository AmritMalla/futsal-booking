package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Futsal")
public class Futsal extends AbstractEntity {


    private static final long serialVersionUID = -1125759616630773271L;

    @Column
    private String futsalName;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String country;

    @Column
    private String city;

    @Column
    private String streetAddress;

    @Column
    private Double rating;

    @ManyToOne
    private Vendor vendor;

    public String getFustsalName() {
        return futsalName;
    }

    public void setFutsalName(String futsalName) {
        this.futsalName = futsalName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Vendor getVendorId() {
        return vendor;
    }

    public void setVendorId(Vendor vendor) {
        this.vendor = vendor;
    }
}
