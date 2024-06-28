package com.identityreconciliation.backend.dto;

import java.util.List;

public class IdentifyResponseDto {
    private Integer primaryContactId;

    private List<String> emails;

    private List<String> phoneNumbers;

    private List<Integer> secondaryContactIds;

    public IdentifyResponseDto() {
    }

    public IdentifyResponseDto(
            Integer primaryContactId,
            List<String> emails,
            List<String> phoneNumbers,
            List<Integer> secondaryContactIds
    ) {
        this.primaryContactId = primaryContactId;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
        this.secondaryContactIds = secondaryContactIds;
    }

    @Override
    public String toString() {
        return "IdentifyResponseDto{" +
                "primaryContactId=" + primaryContactId +
                ", emails=" + emails +
                ", phoneNumbers=" + phoneNumbers +
                ", secondaryContactIds=" + secondaryContactIds +
                '}';
    }

    public Integer getPrimaryContactId() {
        return primaryContactId;
    }

    public void setPrimaryContactId(Integer primaryContactId) {
        this.primaryContactId = primaryContactId;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<Integer> getSecondaryContactIds() {
        return secondaryContactIds;
    }

    public void setSecondaryContactIds(List<Integer> secondaryContactIds) {
        this.secondaryContactIds = secondaryContactIds;
    }
}
