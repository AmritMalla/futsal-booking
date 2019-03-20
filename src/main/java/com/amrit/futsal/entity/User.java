package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "User")
public class User extends AbstractEntity<Long> {

    @Column
    String username;

    @Column
    String email;

    @Column
    private String emailVerificationCode;

    @Column
    String mobileNumber;

    @Column
    String mobileVerificationCode;

    @Column
    String password;

    @Column
    private String passwordResetCode;

    @Column
    private Boolean emailValidated;

    @Column
    private Boolean phoneNumberValidated;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }

    public String getMobileVerificationCode() {
        return mobileVerificationCode;
    }

    public void setMobileVerificationCode(String mobileVerificationCode) {
        this.mobileVerificationCode = mobileVerificationCode;
    }

    public String getPasswordResetCode() {
        return passwordResetCode;
    }

    public void setPasswordResetCode(String passwordResetCode) {
        this.passwordResetCode = passwordResetCode;
    }

    public Boolean getEmailValidated() {
        return emailValidated;
    }

    public void setEmailValidated(Boolean emailValidated) {
        this.emailValidated = emailValidated;
    }

    public Boolean getPhoneNumberValidated() {
        return phoneNumberValidated;
    }

    public void setPhoneNumberValidated(Boolean phoneNumberValidated) {
        this.phoneNumberValidated = phoneNumberValidated;
    }
}
