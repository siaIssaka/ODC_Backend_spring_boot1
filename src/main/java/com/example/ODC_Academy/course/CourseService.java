package com.example.ODC_Academy.course;

import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.CourseDeletionService;
import com.example.ODC_Academy.formation.FormationRepository;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import com.example.ODC_Academy.security.CourseAccess;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final CourseDeletionService courseDeletionService;

    public CourseService(CourseRepository courseRepository,
                        FormationRepository formationRepository,
                        UserRepository userRepository,
                        CourseDeletionService courseDeletionService) {
        this.courseRepository = courseRepository;
        this.formationRepository = formationRepository;
        this.userRepository = userRepository;
        this.courseDeletionService = courseDeletionService;
    }

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", id));
    }

    @Transactional
    public Course create(Long formationId, Long trainerId, CourseRequestDTO request) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Formation", formationId));

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));

        if (trainer.getRole() != Role.FORMATEUR) {
            throw new SecurityException("Seuls les formateurs peuvent créer des cours");
        }

        if (!formation.getTrainers().contains(trainer)) {
            throw new SecurityException("Ce formateur n'est pas autorisé sur cette formation");
        }

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .level(request.level())
                .price(request.price())
                .formation(formation)
                .createdBy(trainer)
                .active(true)
                .build();

        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long courseId, Long trainerId, CourseRequestDTO request) {
        Course course = getById(courseId);
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (!CourseAccess.edits(trainer, course)) {
            throw new AccessDeniedException("Seul le formateur créateur ou le formateur affecté à ce cours historique peut le modifier");
        }
        if (course.getCreatedBy() == null) course.setCreatedBy(trainer);
        course.setTitle(request.title().trim());
        course.setDescription(request.description());
        course.setLevel(request.level().trim());
        course.setPrice(request.price());
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long courseId, Long trainerId) {
        Course course = getById(courseId);
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (!CourseAccess.edits(trainer, course)) {
            throw new AccessDeniedException("Seul le formateur créateur ou le formateur affecté à ce cours historique peut le supprimer");
        }
        courseDeletionService.deleteCourseContent(courseId);
    }
}
