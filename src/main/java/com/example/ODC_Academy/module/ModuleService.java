package com.example.ODC_Academy.module;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import com.example.ODC_Academy.security.CourseAccess;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository,
                         UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<Module> getByCourseId(Long courseId) {
        return courseRepository.findById(courseId)
                .map(course -> moduleRepository.findAll().stream()
                        .filter(module -> module.getCourse() != null && module.getCourse().getId().equals(courseId))
                        .toList())
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
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
