package com.example.ODC_Academy.messaging;

import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Messagerie interne. Périmètre : l'admin écrit à tout le monde ; le formateur écrit à ses apprenants et aux admins ;
 * l'apprenant écrit aux formateurs de ses formations et aux admins.
 */
@Service
@Transactional
public class MessageService {
    public record SendRequest(@NotNull Long recipientId, @NotBlank String subject, @NotBlank String body) { }
    public record Contact(Long id, String name, Role role, String photoKey) { }
    public record MessageDto(Long id, Long senderId, String senderName, Long recipientId, String recipientName,
                             String subject, String body, LocalDateTime sentAt, boolean read) { }

    private final MessageRepository messages;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;
    private final FormationRepository formations;
    private final com.example.ODC_Academy.notification.Notifier notifier;

    public MessageService(MessageRepository m, UserRepository u, EnrollmentRepository e, FormationRepository f,
                          com.example.ODC_Academy.notification.Notifier n) {
        this.notifier = n;
        this.messages = m; this.users = u; this.enrollments = e; this.formations = f;
    }

    private static String name(User u) { return u.getPrenom() + " " + u.getNom(); }

    @Transactional(readOnly = true)
    public List<Contact> contacts(User me) {
        Map<Long, User> out = new LinkedHashMap<>();
        if (me.getRole() == Role.ADMIN) {
            users.findAll().forEach(u -> out.put(u.getId(), u));
        } else {
            users.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).forEach(u -> out.put(u.getId(), u));
            if (me.getRole() == Role.APPRENANT) {
                for (Enrollment e : enrollments.findByUserId(me.getId())) {
                    Formation f = e.getCourse().getFormation();
                    if (f != null) f.getTrainers().forEach(t -> out.put(t.getId(), t));
                }
            } else {
                for (Formation f : formations.findAll()) {
                    if (!CourseAccess.trains(me, f)) continue;
                    f.getCourses().forEach(c -> enrollments.findByCourseId(c.getId()).forEach(e -> out.put(e.getUser().getId(), e.getUser())));
                }
            }
        }
        out.remove(me.getId());
        return out.values().stream().filter(User::isActive)
                .map(u -> new Contact(u.getId(), name(u), u.getRole(), u.getPhotoKey())).toList();
    }

    public MessageDto send(SendRequest r, User me) {
        if (contacts(me).stream().noneMatch(c -> c.id().equals(r.recipientId())))
            throw new AccessDeniedException("Vous ne pouvez pas écrire à ce destinataire");
        User to = users.findById(r.recipientId()).orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", r.recipientId()));
        notifier.newMessage(to, name(me));
        return dto(messages.save(Message.builder().sender(me).recipient(to).subject(r.subject().trim())
                .body(r.body().trim()).sentAt(LocalDateTime.now()).build()));
    }

    @Transactional(readOnly = true)
    public List<MessageDto> inbox(User me) { return messages.findByRecipientIdOrderBySentAtDesc(me.getId()).stream().map(this::dto).toList(); }

    @Transactional(readOnly = true)
    public List<MessageDto> sent(User me) { return messages.findBySenderIdOrderBySentAtDesc(me.getId()).stream().map(this::dto).toList(); }

    @Transactional(readOnly = true)
    public long unread(User me) { return messages.countByRecipientIdAndReadAtIsNull(me.getId()); }

    public void markRead(Long id, User me) {
        Message m = messages.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Message", id));
        if (!m.getRecipient().getId().equals(me.getId())) throw new AccessDeniedException("Message d'un autre utilisateur");
        if (m.getReadAt() == null) m.setReadAt(LocalDateTime.now());
    }

    private MessageDto dto(Message m) {
        return new MessageDto(m.getId(), m.getSender().getId(), name(m.getSender()), m.getRecipient().getId(),
                name(m.getRecipient()), m.getSubject(), m.getBody(), m.getSentAt(), m.getReadAt() != null);
    }
}
