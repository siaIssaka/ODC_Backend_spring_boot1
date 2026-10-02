package com.example.ODC_Academy.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientIdOrderBySentAtDesc(Long id);
    List<Message> findBySenderIdOrderBySentAtDesc(Long id);
    long countByRecipientIdAndReadAtIsNull(Long id);
}
