package com.identityreconciliation.backend.dto;

public class IdentifyRequestDto {
    private String email;

    private String phoneNumber;

    public IdentifyRequestDto() {
    }

    public IdentifyRequestDto(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "IdentifyRequestDto{" +
                "email='" + email + '\'' +
                ", phoneNumber=" + phoneNumber +
                '}';
    }
}
