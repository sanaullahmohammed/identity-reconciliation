package com.identityreconciliation.backend.repository;

import com.identityreconciliation.backend.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IContactRepository extends JpaRepository<Contact, Integer> {
    Contact findOneByEmailAndPhoneNumber(String email, String phoneNumber);

    List<Contact> findAllByLinkedId(Integer linkedId);

    List<Contact> findAllByEmail(String email);

    List<Contact> findAllByPhoneNumber(String phoneNumber);
}
