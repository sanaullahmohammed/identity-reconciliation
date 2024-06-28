package com.identityreconciliation.backend.controller;

import com.identityreconciliation.backend.dto.ContactDto;
import com.identityreconciliation.backend.dto.IdentifyRequestDto;
import com.identityreconciliation.backend.dto.IdentifyResponseDto;
import com.identityreconciliation.backend.enums.LinkPrecedence;
import com.identityreconciliation.backend.service.IContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.*;

@RestController
@RequestMapping("/api/identify")
public class IdentifyController {
    Logger logger = LoggerFactory.getLogger(IdentifyController.class);
    @Autowired
    private IContactService _contactService;

    @PostMapping
    public ResponseEntity<IdentifyResponseDto> index(@RequestBody IdentifyRequestDto identifyRequestDto) {
        logger.info(MessageFormat.format("POST request received with body: {0}", identifyRequestDto.toString()));

        if (identifyRequestDto.getEmail() == null
                && identifyRequestDto.getPhoneNumber() == null) {
            logger.info("Request cannot be processed since contact-email and contact-phoneNumber values are null");

            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }

        var receivedContactDto = new ContactDto();

        receivedContactDto.setEmail(identifyRequestDto.getEmail());
        receivedContactDto.setPhoneNumber(identifyRequestDto.getPhoneNumber());

        var primaryContactDtos = _contactService.determinePrimaryContactDtos(receivedContactDto);

        logger.info(MessageFormat
                .format("Found {0} primary contacts, {1}",
                        primaryContactDtos.size(),
                        Arrays.toString(primaryContactDtos.toArray())));

        if (primaryContactDtos.size() == 0) {
            receivedContactDto.setLinkedId(null);
            receivedContactDto.setLinkPrecedence(LinkPrecedence.PRIMARY);

            logger.info(MessageFormat.format("Creating a new contact: {0}...", receivedContactDto.toString()));
            var createdContactDto = _contactService.createNewContact(receivedContactDto);
            logger.info(MessageFormat.format("Created a new contact: {0}", createdContactDto.toString()));

            var identifyResponseDto = new IdentifyResponseDto(
                    createdContactDto.getId(),
                    createdContactDto.getEmail() == null
                            ? Collections.emptyList()
                            : List.of(createdContactDto.getEmail()),
                    createdContactDto.getPhoneNumber() == null
                            ? Collections.emptyList()
                            : List.of(createdContactDto.getPhoneNumber()),
                    Collections.emptyList());

            return new ResponseEntity<>(identifyResponseDto, HttpStatus.OK);
        } else if (primaryContactDtos.size() == 1) {
            var primaryContactDto = primaryContactDtos.stream().findFirst().get();

            logger.info(MessageFormat.format("Checking if contact: {0} already exists...",
                    receivedContactDto.toString()));
            var isContactDtoExists = _contactService.isContactExists(receivedContactDto);

            if (!isContactDtoExists) {
                logger.info(MessageFormat
                        .format("Creating a new contact: {0} since it doesn't exist...",
                                receivedContactDto.toString()));
                receivedContactDto.setLinkedId(primaryContactDto.getId());
                receivedContactDto.setLinkPrecedence(LinkPrecedence.SECONDARY);

                var createdContactDto = _contactService.createNewContact(receivedContactDto);
                logger.info(MessageFormat.format("Created a new contact: {0}", createdContactDto.toString()));
            }

            var secondaryContactDtos = _contactService
                    .getSecondaryContactDtosForGivenPrimaryContactId(primaryContactDto.getId());
            logger.info(MessageFormat
                    .format("Fetched {0} contacts which are secondary to {1}, {2}",
                            secondaryContactDtos.size(),
                            primaryContactDto.toString(),
                            Arrays.toString(secondaryContactDtos.toArray())));

            var emails = new LinkedHashSet<String>();
            emails.add(primaryContactDto.getEmail());
            emails.addAll(secondaryContactDtos.stream().map(ContactDto::getEmail).toList());

            var phoneNumbers = new LinkedHashSet<String>();
            phoneNumbers.add(primaryContactDto.getPhoneNumber());
            phoneNumbers.addAll(secondaryContactDtos.stream().map(ContactDto::getPhoneNumber).toList());

            var identifyReponseDto = new IdentifyResponseDto(
                    primaryContactDto.getId(),
                    emails.stream().filter(Objects::nonNull).toList(),
                    phoneNumbers.stream().filter(Objects::nonNull).toList(),
                    secondaryContactDtos.stream().map(ContactDto::getId).toList()
            );

            return new ResponseEntity<>(identifyReponseDto, HttpStatus.OK);
        } else {
            // sort in increasing order of creation date-time
            var sortedPrimaryContactDtos = new ArrayList<>(primaryContactDtos);
            sortedPrimaryContactDtos.sort(Comparator.comparing(ContactDto::getCreatedAt));

            var oldestPrimaryContactDto = sortedPrimaryContactDtos.get(0);

            var toBeReconciledPrimaryContactDtos = sortedPrimaryContactDtos.stream().skip(1).toList();

            logger.info(MessageFormat
                    .format("Reconciling contacts: {0}, with regards to primary-contact: {1}",
                            Arrays.toString(toBeReconciledPrimaryContactDtos.toArray()),
                            oldestPrimaryContactDto.toString()));
            var reconciledContactDtos = _contactService.reconcilePrimaryContacts(
                    toBeReconciledPrimaryContactDtos,
                    oldestPrimaryContactDto);
            logger.info(MessageFormat
                    .format("Reconciled contacts: {0}, with regards to primary-contact: {1}",
                            Arrays.toString(reconciledContactDtos.toArray()),
                            oldestPrimaryContactDto.toString()));

            var secondaryContactDtos = _contactService
                    .getSecondaryContactDtosForGivenPrimaryContactId(oldestPrimaryContactDto.getId());
            logger.info(MessageFormat
                    .format("Fetched secondary-contacts: {0}, with regards to primary-contact: {1}",
                            Arrays.toString(secondaryContactDtos.toArray()),
                            oldestPrimaryContactDto.toString()));

            var emails = new LinkedHashSet<String>();
            emails.add(oldestPrimaryContactDto.getEmail());
            emails.addAll(secondaryContactDtos.stream().map(ContactDto::getEmail).toList());

            var phoneNumbers = new LinkedHashSet<String>();
            phoneNumbers.add(oldestPrimaryContactDto.getPhoneNumber());
            phoneNumbers.addAll(secondaryContactDtos.stream().map(ContactDto::getPhoneNumber).toList());

            var identifyResponseDto = new IdentifyResponseDto(
                    oldestPrimaryContactDto.getId(),
                    emails.stream().filter(Objects::nonNull).toList(),
                    phoneNumbers.stream().filter(Objects::nonNull).toList(),
                    secondaryContactDtos.stream().map(ContactDto::getId).toList()
            );

            return new ResponseEntity<>(identifyResponseDto, HttpStatus.OK);
        }
    }
}
