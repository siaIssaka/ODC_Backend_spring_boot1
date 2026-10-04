package com.example.ODC_Academy.module;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.security.CourseContentAccess;
import com.example.ODC_Academy.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseContentAccess contentAccess;
    private final SecurityUtils securityUtils;

    public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository,
                         UserRepository userRepository, CourseContentAccess contentAccess,
                         SecurityUtils securityUtils) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.contentAccess = contentAccess;
        this.securityUtils = securityUtils;
    }

    public List<Module> getByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        contentAccess.assertCanView(securityUtils.getCurrentUser(), course);
        return moduleRepository.findAll().stream()
                .filter(module -> module.getCourse() != null && module.getCourse().getId().equals(courseId))
                .toList();
    }

    @Transactional
    public Module create(Long courseId, Long trainerId, ModuleRequestDTO request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (trainer.getRole() != Role.FORMATEUR) {
            throw new SecurityException("Seuls les formateurs peuvent créer des modules");
        }

        if (!CourseAccess.edits(trainer, course)) {
            throw new AccessDeniedException("Seul le formateur créateur peut modifier le contenu de ce cours");
        }

        Module module = Module.builder()
                .title(request.title())
                .description(request.description())
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : 0)
                .course(course)
                .build();

        return moduleRepository.save(module);
    }
}
