package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Futsal")
public class Futsal extends AbstractEntity<Long>{

    @Column
    private String fustsalName;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column
    private String country;

    @Column
    private String city;

    @Column
    private String streetAddress;

    @Column
    private double rating;

    @Column
    private Long vendorId;

    public String getFustsalName() {
        return fustsalName;
    }

    public void setFustsalName(String fustsalName) {
        this.fustsalName = fustsalName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
}
