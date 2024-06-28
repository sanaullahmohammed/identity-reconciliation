package com.identityreconciliation.backend.mapper;

import com.identityreconciliation.backend.dto.ContactDto;
import com.identityreconciliation.backend.model.Contact;

public class ContactMapper {
    public static ContactDto mapToContactDto(Contact contact) {
        return new ContactDto(
                contact.getId(),
                contact.getPhoneNumber(),
                contact.getEmail(),
                contact.getLinkedId(),
                contact.getLinkPrecedence(),
                contact.getCreatedAt(),
                contact.getUpdatedAt(),
                contact.getDeletedAt()
        );
    }

    public static Contact mapToContact(ContactDto contactDto) {
        return new Contact(
                contactDto.getId(),
                contactDto.getPhoneNumber(),
                contactDto.getEmail(),
                contactDto.getLinkedId(),
                contactDto.getLinkPrecedence(),
                contactDto.getCreatedAt(),
                contactDto.getUpdatedAt(),
                contactDto.getDeletedAt()
        );
    }
}
