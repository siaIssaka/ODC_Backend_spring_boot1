package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.enrollment.Enrollment;
import com.example.ODC_Academy.enrollment.EnrollmentRepository;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.lesson.LessonRepository;
import com.example.ODC_Academy.progress.ProgressRepository;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/** Tableau des formateurs pour l'admin : profil, formations, contenu publié et avancement des apprenants. */
@RestController
@RequestMapping("/api/v1/admin/trainers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrainerController {
    public record CourseOverview(Long id, String title) { }
    public record FormationOverview(Long id, String title, List<CourseOverview> courses) { }
    public record TrainerOverview(Long id, String prenom, String nom, String email, String photoKey, boolean active,
                                  List<String> formations, List<FormationOverview> formationDetails,
                                  int courses, int lessons, int learners, double avgLearnerProgress) { }

    private final UserRepository users;
    private final FormationRepository formations;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final ProgressRepository progress;

    public AdminTrainerController(UserRepository u, FormationRepository f, LessonRepository l, EnrollmentRepository e, ProgressRepository p) {
        this.users = u; this.formations = f; this.lessons = l; this.enrollments = e; this.progress = p;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TrainerOverview> list() {
        List<TrainerOverview> out = new ArrayList<>();
        for (User t : users.findAll()) {
            if (t.getRole() != Role.FORMATEUR) continue;
            List<String> titles = new ArrayList<>();
            List<FormationOverview> formationDetails = new ArrayList<>();
            Set<Long> learners = new HashSet<>();
            int courses = 0, lessonCount = 0, enrolled = 0;
            double sum = 0;
            for (Formation f : formations.findByTrainersContaining(t)) {
                titles.add(f.getTitle());
                formationDetails.add(new FormationOverview(f.getId(), f.getTitle(), f.getCourses().stream()
                        .sorted(Comparator.comparing(Course::getTitle))
                        .map(c -> new CourseOverview(c.getId(), c.getTitle()))
                        .toList()));
                for (Course c : f.getCourses()) {
                    courses++;
                    lessonCount += lessons.findByCourseIdOrderByOrderIndexAsc(c.getId()).size();
                    for (Enrollment e : enrollments.findByCourseId(c.getId())) {
                        enrolled++;
                        learners.add(e.getUser().getId());
                        sum += progress.findByUserIdAndCourseId(e.getUser().getId(), c.getId()).map(p -> p.getPercentage()).orElse(0.0);
                    }
                }
            }
            out.add(new TrainerOverview(t.getId(), t.getPrenom(), t.getNom(), t.getEmail(), t.getPhotoKey(), t.isActive(),
                    titles, formationDetails, courses, lessonCount, learners.size(),
                    enrolled == 0 ? 0 : Math.round(sum / enrolled * 10) / 10.0));
        }
        return out;
    }
}
