package com.example.ODC_Academy.formation;

import com.example.ODC_Academy.category.CategoryRepository;
import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormationService {

    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseDeletionService courseDeletionService;

    public FormationService(FormationRepository formationRepository,
                            UserRepository userRepository,
                            CourseRepository courseRepository,
                            CategoryRepository categoryRepository,
                            CourseDeletionService courseDeletionService) {
        this.categoryRepository = categoryRepository;
        this.formationRepository = formationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseDeletionService = courseDeletionService;
    }

    public List<Formation> getAll() {
        return formationRepository.findAll();
    }

    public List<Formation> getForTrainer(Long trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (trainer.getRole() != Role.FORMATEUR) {
            throw new SecurityException("Ce compte n'est pas un formateur");
        }
        return formationRepository.findByTrainersContaining(trainer);
    }

    public Formation getById(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Formation", id));
    }

    @Transactional
    public Formation create(FormationRequestDTO request, Long adminId) {
        requireAdmin(adminId);

        Formation formation = Formation.builder()
                .title(request.title())
                .description(request.description())
                .active(true)
                .category(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Catégorie", request.categoryId())))
                .build();

        return formationRepository.save(formation);
    }

    @Transactional
    public Formation update(Long formationId, FormationRequestDTO request, Long adminId) {
        requireAdmin(adminId);
        Formation formation = getById(formationId);
        formation.setTitle(request.title().trim());
        formation.setDescription(request.description());
        formation.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Catégorie", request.categoryId())));
        return formationRepository.save(formation);
    }

    @Transactional
    public void delete(Long formationId, Long adminId) {
        requireAdmin(adminId);
        Formation formation = getById(formationId);
        courseDeletionService.deleteFormationContent(formationId, formation.getImageKey());
    }

    @Transactional
    public Formation addTrainer(Long formationId, Long trainerId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", adminId));
        if (admin.getRole() != Role.ADMIN) {
            throw new SecurityException("Seuls les administrateurs peuvent affecter des formateurs");
        }

        Formation formation = getById(formationId);
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (trainer.getRole() != Role.FORMATEUR) {
            throw new SecurityException("Seuls les formateurs peuvent être affectés à une formation");
        }

        formation.getTrainers().add(trainer);
        return formationRepository.save(formation);
    }

    @Transactional
    public Formation addCourse(Long formationId, Long courseId, Long trainerId) {
        Formation formation = getById(formationId);
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", trainerId));
        if (trainer.getRole() != Role.FORMATEUR) {
            throw new SecurityException("Seuls les formateurs peuvent créer des cours");
        }
        if (!formation.getTrainers().contains(trainer)) {
            throw new SecurityException("Ce formateur n'est pas autorisé sur cette formation");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));
        course.setFormation(formation);
        courseRepository.save(course);
        return formationRepository.save(formation);
    }

    private void requireAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", adminId));
        if (admin.getRole() != Role.ADMIN) {
            throw new SecurityException("Seuls les administrateurs peuvent gérer les formations");
        }
    }
}
