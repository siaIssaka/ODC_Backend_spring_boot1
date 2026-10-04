package com.example.ODC_Academy.assignment;

import com.example.ODC_Academy.assignment.AssignmentDtos.*;
import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.media.MediaStorageService;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.security.CourseContentAccess;
import com.example.ODC_Academy.status.EnrollmentStatus;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssignmentService {
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final MediaStorageService storage;
    private final CourseContentAccess contentAccess;

    public AssignmentService(AssignmentRepository a, SubmissionRepository s, CourseRepository c,
                             EnrollmentRepository e, MediaStorageService m, CourseContentAccess contentAccess) {
        this.assignments = a; this.submissions = s; this.courses = c; this.enrollments = e;
        this.storage = m; this.contentAccess = contentAccess;
    }

    /** ADMIN, ou formateur affecté à la formation du cours. */
    private void assertManager(User u, Course c) {
        if (!CourseAccess.manages(u, c)) throw new AccessDeniedException("Vous ne gérez pas ce cours");
    }

    private void assertEditor(User u, Course c) {
        if (!CourseAccess.edits(u, c)) throw new AccessDeniedException("Seul le formateur créateur peut modifier ce cours");
    }

    private Assignment find(Long id) {
        return assignments.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Devoir", id));
    }

    private AssignmentDto toDto(Assignment a, User viewer) {
        Submission s = viewer.getRole() == Role.APPRENANT
                ? submissions.findByAssignmentIdAndLearnerId(a.getId(), viewer.getId()).orElse(null) : null;
        return new AssignmentDto(a.getId(), a.getCourse().getId(), a.getCourse().getTitle(), a.getTitle(),
                a.getDescription(), a.getOpensAt(), a.getClosesAt(), a.status(LocalDateTime.now()),
                s != null, s == null ? null : s.getGrade());
    }

    public AssignmentDto create(Long courseId, AssignmentRequest r, User me) {
        Course c = courses.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        assertEditor(me, c);
        if (!r.closesAt().isAfter(r.opensAt())) throw new BadRequestException("La fermeture doit suivre l'ouverture");
        Assignment a = assignments.save(Assignment.builder().course(c).title(r.title().trim())
                .description(r.description()).opensAt(r.opensAt()).closesAt(r.closesAt()).build());
        return toDto(a, me);
    }

    public AssignmentDto update(Long id, AssignmentRequest r, User me) {
        Assignment a = find(id);
        assertEditor(me, a.getCourse());
        if (!"PLANIFIE".equals(a.status(LocalDateTime.now()))) {
            throw new BadRequestException("Seul un devoir planifié peut être modifié");
        }
        if (submissions.countByAssignmentId(id) > 0) {
            throw new BadRequestException("Ce devoir ne peut plus être modifié car un apprenant a déjà rendu son travail");
        }
        if (!r.closesAt().isAfter(r.opensAt())) throw new BadRequestException("La fermeture doit suivre l'ouverture");
        a.setTitle(r.title().trim());
        a.setDescription(r.description());
        a.setOpensAt(r.opensAt());
        a.setClosesAt(r.closesAt());
        return toDto(assignments.save(a), me);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> forCourse(Long courseId, User me) {
        Course course = courses.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        contentAccess.assertCanView(me, course);
        return assignments.findByCourseIdOrderByClosesAtAsc(courseId).stream().map(a -> toDto(a, me)).toList();
    }

    /** Devoirs de tous les cours où l'apprenant est inscrit (widget « À venir »). */
    @Transactional(readOnly = true)
    public List<AssignmentDto> mine(User me) {
        List<Long> ids = enrollments.findByUserId(me.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(e -> e.getCourse().getId()).toList();
        if (ids.isEmpty()) return List.of();
        return assignments.findByCourseIdInOrderByClosesAtAsc(ids).stream().map(a -> toDto(a, me)).toList();
    }

    public AssignmentDto submit(Long id, MultipartFile file, String text, User me) {
        Assignment a = find(id);
        if (me.getRole() != Role.APPRENANT) throw new AccessDeniedException("Réservé aux apprenants");
        Enrollment en = enrollments.findByUserIdAndCourseId(me.getId(), a.getCourse().getId())
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).orElse(null);
        if (en == null) throw new AccessDeniedException("Vous n'êtes pas inscrit activement à ce cours");
        if (!"OUVERT".equals(a.status(LocalDateTime.now()))) throw new BadRequestException("Cet espace de dépôt est fermé");
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasText = text != null && !text.isBlank();
        if (hasFile == hasText) {
            throw new BadRequestException("Déposez un fichier ou saisissez une réponse texte, mais pas les deux");
        }
        String key = hasFile ? storage.storeSubmission(file) : storage.storeTextSubmission(text);
        Submission s = submissions.findByAssignmentIdAndLearnerId(id, me.getId()).orElse(null);
        if (s != null) storage.delete(s.getFileKey()); // re-dépôt autorisé tant que l'espace est ouvert
        else s = Submission.builder().assignment(a).learner(me).build();
        s.setFileKey(key);
        s.setOriginalName(hasFile ? file.getOriginalFilename() : "Réponse texte.txt");
        s.setSubmittedAt(LocalDateTime.now());
        s.setGrade(null); s.setFeedback(null);
        submissions.save(s);
        return toDto(a, me);
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> submissionsOf(Long id, User me) {
        Assignment a = find(id);
        assertManager(me, a.getCourse());
        return submissions.findByAssignmentId(id).stream().map(s -> new SubmissionDto(s.getId(), s.getLearner().getId(),
                s.getLearner().getPrenom() + " " + s.getLearner().getNom(), s.getOriginalName(),
                s.getSubmittedAt(), s.getGrade(), s.getFeedback())).toList();
    }

    public void grade(Long submissionId, GradeRequest r, User me) {
        Submission s = submissions.findById(submissionId).orElseThrow(() -> ResourceNotFoundException.of("Dépôt", submissionId));
        assertEditor(me, s.getAssignment().getCourse());
        if (r.grade() < 0 || r.grade() > 20) throw new BadRequestException("La note doit être entre 0 et 20");
        s.setGrade(r.grade()); s.setFeedback(r.feedback());
    }

    /** Fichier déposé : propriétaire ou gestionnaire du cours uniquement (pas de lien public). */
    @Transactional(readOnly = true)
    public Submission fileOf(Long submissionId, User me) {
        Submission s = submissions.findById(submissionId).orElseThrow(() -> ResourceNotFoundException.of("Dépôt", submissionId));
        if (!s.getLearner().getId().equals(me.getId())) assertManager(me, s.getAssignment().getCourse());
        return s;
    }
}
