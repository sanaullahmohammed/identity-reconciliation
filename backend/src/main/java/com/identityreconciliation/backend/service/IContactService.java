package com.identityreconciliation.backend.service;

import com.identityreconciliation.backend.dto.ContactDto;

import java.util.List;

public interface IContactService {
    List<ContactDto> reconcilePrimaryContacts(List<ContactDto> toBeReconciledPrimaryContactDtos, ContactDto primaryContactDto);

    List<ContactDto> determinePrimaryContactDtos(ContactDto contactDto);

    List<ContactDto> getSecondaryContactDtosForGivenPrimaryContactId(Integer primaryContactId);

    Boolean isContactExists(ContactDto contactDto);

    ContactDto createNewContact(ContactDto contactDto);
}
