package com.example.ODC_Academy.forum;

import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Forum par formation : admin, formateurs affectés et apprenants inscrits à un cours de cette formation. */
@Service
@Transactional
public class ForumService {
    public record ThreadRequest(@NotBlank String title, @NotBlank String body) { }
    public record ReplyRequest(@NotBlank String body) { }
    public record PostDto(Long id, Long authorId, String title, String body, String authorName, Role authorRole, String authorPhotoKey,
                          LocalDateTime createdAt, long replies) { }
    public record ThreadView(PostDto thread, List<PostDto> replies) { }

    private final ForumPostRepository posts;
    private final FormationRepository formations;
    private final EnrollmentRepository enrollments;
    private final com.example.ODC_Academy.notification.Notifier notifier;

    public ForumService(ForumPostRepository p, FormationRepository f, EnrollmentRepository e,
                        com.example.ODC_Academy.notification.Notifier n) {
        this.notifier = n;
        this.posts = p; this.formations = f; this.enrollments = e;
    }

    private Formation formation(Long id, User me) {
        Formation f = formations.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Formation", id));
        boolean ok = me.getRole() == Role.ADMIN || CourseAccess.trains(me, f)
                || (me.getRole() == Role.APPRENANT && enrollments.findByUserId(me.getId()).stream()
                .anyMatch(e -> e.getCourse().getFormation() != null && e.getCourse().getFormation().getId().equals(id)));
        if (!ok) throw new AccessDeniedException("Forum réservé aux inscrits de cette formation");
        return f;
    }

    private PostDto dto(ForumPost p) {
        User a = p.getAuthor();
        return new PostDto(p.getId(), a.getId(), p.getTitle(), p.getBody(), a.getPrenom() + " " + a.getNom(), a.getRole(),
                a.getPhotoKey(), p.getCreatedAt(), posts.countByParentId(p.getId()));
    }

    @Transactional(readOnly = true)
    public List<PostDto> threads(Long formationId, User me) {
        formation(formationId, me);
        return posts.findByFormationIdAndParentIsNullOrderByCreatedAtDesc(formationId).stream().map(this::dto).toList();
    }

    public PostDto createThread(Long formationId, ThreadRequest r, User me) {
        Formation f = formation(formationId, me);
        return dto(posts.save(ForumPost.builder().formation(f).author(me).title(r.title().trim())
                .body(r.body().trim()).createdAt(LocalDateTime.now()).build()));
    }

    @Transactional(readOnly = true)
    public ThreadView thread(Long threadId, User me) {
        ForumPost t = posts.findById(threadId).orElseThrow(() -> ResourceNotFoundException.of("Discussion", threadId));
        formation(t.getFormation().getId(), me);
        return new ThreadView(dto(t), posts.findByParentIdOrderByCreatedAtAsc(threadId).stream().map(this::dto).toList());
    }

    public PostDto reply(Long threadId, ReplyRequest r, User me) {
        ForumPost t = posts.findById(threadId).orElseThrow(() -> ResourceNotFoundException.of("Discussion", threadId));
        formation(t.getFormation().getId(), me);
        if (!t.getAuthor().getId().equals(me.getId())) {
            t.getAuthor().getEmail(); // initialise le proxy LAZY avant de le passer au thread asynchrone
            notifier.forumReply(t.getAuthor(), me.getPrenom() + " " + me.getNom(), t.getTitle());
        }
        return dto(posts.save(ForumPost.builder().formation(t.getFormation()).author(me).parent(t)
                .body(r.body().trim()).createdAt(LocalDateTime.now()).build()));
    }
}
