package com.identityreconciliation.backend.service;

import com.identityreconciliation.backend.dto.ContactDto;
import com.identityreconciliation.backend.enums.LinkPrecedence;
import com.identityreconciliation.backend.mapper.ContactMapper;
import com.identityreconciliation.backend.model.Contact;
import com.identityreconciliation.backend.repository.IContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class ContactService implements IContactService {
    Logger logger = LoggerFactory.getLogger(ContactService.class);
    @Autowired
    private IContactRepository _contactRepository;

    @Override
    public List<ContactDto> getSecondaryContactDtosForGivenPrimaryContactId(Integer primaryContactId) {
        var secondaryContactDtos = _contactRepository.findAllByLinkedId(primaryContactId);

        return secondaryContactDtos
                .stream()
                .filter(secondaryContactDto -> secondaryContactDto.getDeletedAt() == null)
                .map(ContactMapper::mapToContactDto)
                .toList();
    }

    @Override
    public List<ContactDto> determinePrimaryContactDtos(ContactDto contactDto) {
        logger.info(MessageFormat.format("Fetching all contacts by email: {0}", contactDto.getEmail()));
        var similarEmailContacts = contactDto.getEmail() != null
                ? _contactRepository.findAllByEmail(contactDto.getEmail())
                : Collections.<Contact> emptyList();
        var similarEmailValidContacts = similarEmailContacts
                .stream()
                .filter(contact -> contact.getDeletedAt() == null)
                .toList();
        logger.info(MessageFormat
                .format("Fetched {0} contacts by email: {1}, {2}",
                        similarEmailValidContacts.size(),
                        contactDto.getEmail(),
                        Arrays.toString(similarEmailValidContacts.toArray())));

        logger.info(MessageFormat
                .format("Fetching all contacts by phoneNumber: {0}", contactDto.getPhoneNumber()));
        var similarPhoneNumberContacts = contactDto.getPhoneNumber() != null
                ? _contactRepository.findAllByPhoneNumber(contactDto.getPhoneNumber())
                : Collections.<Contact> emptyList();
        var similarPhoneNumberValidContacts = similarPhoneNumberContacts
                .stream()
                .filter(contact -> contact.getDeletedAt() == null)
                .toList();
        logger.info(MessageFormat
                .format("Fetched {0} contacts by phoneNumber: {1}, {2}",
                        similarPhoneNumberValidContacts.size(),
                        contactDto.getPhoneNumber(),
                        Arrays.toString(similarPhoneNumberValidContacts.toArray())));

        var primaryContactDtoIds = new HashSet<Integer>();

        for (var contact: similarEmailValidContacts) {
            if (contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
                primaryContactDtoIds.add(contact.getId());
            } else {
                primaryContactDtoIds.add(contact.getLinkedId());
            }
        }
        for (var contact: similarPhoneNumberValidContacts) {
            if (contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
                primaryContactDtoIds.add(contact.getId());
            } else {
                primaryContactDtoIds.add(contact.getLinkedId());
            }
        }

        if (primaryContactDtoIds.size() == 0) {
            logger.info("Identified no primary contacts");

            return List.of();
        } else if (primaryContactDtoIds.size() == 1) {
            var primaryContactId = primaryContactDtoIds.stream().findFirst().get();
            var primaryContact = _contactRepository.findById(primaryContactId).orElse(null);

            assert primaryContact != null;
            var validPrimaryContact = primaryContact.getDeletedAt() == null ? primaryContact : null;

            assert validPrimaryContact != null;
            logger.info(MessageFormat
                    .format("Identified the following primary-contact: {0}", validPrimaryContact.toString()));

            return List.of(ContactMapper.mapToContactDto(validPrimaryContact));
        } else {
            var primaryContacts = new ArrayList<Contact>(primaryContactDtoIds.size());

            for (var primaryContactId: primaryContactDtoIds) {
                var primaryContact = _contactRepository.findById(primaryContactId).orElse(null);

                assert primaryContact != null;
                if (primaryContact.getDeletedAt() == null) {
                    primaryContacts.add(primaryContact);
                }
            }

            logger.info(MessageFormat.format("Identified the following primary-contacts: {0}",
                    Arrays.toString(primaryContacts.toArray())));

            return primaryContacts
                    .stream()
                    .map(ContactMapper::mapToContactDto)
                    .toList();
        }
    }

    @Override
    public List<ContactDto> reconcilePrimaryContacts(
            List<ContactDto> toBeReconciledPrimaryContactDtos,
            ContactDto primaryContactDto) {
        var reconciledContactDtos = new LinkedList<ContactDto>();

        for (var toBeReconciledPrimaryContactDto: toBeReconciledPrimaryContactDtos) {
            var secondaryContacts = _contactRepository.findAllByLinkedId(toBeReconciledPrimaryContactDto.getId());
            var secondaryValidContacts = secondaryContacts
                    .stream()
                    .filter(contact -> contact.getDeletedAt() == null)
                    .toList();
            var secondaryContactDtos = secondaryValidContacts
                    .stream()
                    .map(ContactMapper::mapToContactDto)
                    .toList();

            var toBeReconciledContactDtos = new LinkedList<ContactDto>();
            toBeReconciledContactDtos.add(toBeReconciledPrimaryContactDto);
            toBeReconciledContactDtos.addAll(secondaryContactDtos);

            for (var toBeReconciledContactDto: toBeReconciledContactDtos) {
                logger.info(MessageFormat.format("Reconciling contact: {0}", toBeReconciledContactDto.toString()));

                toBeReconciledContactDto.setLinkedId(primaryContactDto.getId());
                toBeReconciledContactDto.setLinkPrecedence(LinkPrecedence.SECONDARY);

                var updatedContact = _contactRepository.save(ContactMapper.mapToContact(toBeReconciledContactDto));

                reconciledContactDtos.add(ContactMapper.mapToContactDto(updatedContact));
            }
        }

        return reconciledContactDtos;
    }

    @Override
    public Boolean isContactExists(ContactDto contactDto) {
        var contactFromDb = _contactRepository.findOneByEmailAndPhoneNumber(contactDto.getEmail(), contactDto.getPhoneNumber());

        if (contactFromDb == null) {
            logger.info(MessageFormat
                    .format("Fetched no contact by email: {0} and phoneNumber: {1}",
                            contactDto.getEmail(),
                            contactDto.getPhoneNumber()));

            var primaryContactDtos = determinePrimaryContactDtos(contactDto);

            var similarEmailContacts = contactDto.getEmail() != null
                    ? _contactRepository.findAllByEmail(contactDto.getEmail())
                    : Collections.<Contact> emptyList();
            var similarEmailValidContacts = similarEmailContacts
                    .stream()
                    .filter(contact -> contact.getDeletedAt() == null)
                    .toList();
            logger.info(MessageFormat
                    .format("Fetched {0} contacts by email: {1}, {2}",
                            similarEmailValidContacts.size(),
                            contactDto.getEmail(),
                            Arrays.toString(similarEmailValidContacts.toArray())));

            logger.info(MessageFormat
                    .format("Fetching all contacts by phoneNumber: {0}", contactDto.getPhoneNumber()));
            var similarPhoneNumberContacts = contactDto.getPhoneNumber() != null
                    ? _contactRepository.findAllByPhoneNumber(contactDto.getPhoneNumber())
                    : Collections.<Contact> emptyList();
            var similarPhoneNumberValidContacts = similarPhoneNumberContacts
                    .stream()
                    .filter(contact -> contact.getDeletedAt() == null)
                    .toList();
            logger.info(MessageFormat
                    .format("Fetched {0} contacts by phoneNumber: {1}, {2}",
                            similarPhoneNumberValidContacts.size(),
                            contactDto.getPhoneNumber(),
                            Arrays.toString(similarEmailValidContacts.toArray())));

            return primaryContactDtos.size() == 1
                    && similarEmailValidContacts.size() == 1
                    && similarPhoneNumberValidContacts.size() == 1;
        } else {
            logger.info(MessageFormat
                    .format("Fetched contact {0} by email: {1} and phoneNumber: {2}",
                            contactDto.toString(),
                            contactDto.getEmail(),
                            contactDto.getPhoneNumber()));

            return true;
        }
    }

    @Override
    public ContactDto createNewContact(ContactDto contactDto) {
        var contact = ContactMapper.mapToContact(contactDto);
        var savedContact = _contactRepository.save(contact);

        return ContactMapper.mapToContactDto(savedContact);
    }
}
