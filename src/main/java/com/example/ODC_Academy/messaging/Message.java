package com.example.ODC_Academy.messaging;

import com.example.ODC_Academy.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_id")
    private User recipient;
    @Column(nullable = false)
    private String subject;
    @Column(nullable = false, length = 4000)
    private String body;
    @Column(nullable = false)
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
