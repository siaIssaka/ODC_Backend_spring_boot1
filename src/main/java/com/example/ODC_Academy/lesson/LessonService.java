package com.example.ODC_Academy.lesson;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.course.CourseRepository;
import com.example.ODC_Academy.lesson.LessonRequestDTO;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.module.Module;
import com.example.ODC_Academy.module.ModuleRepository;
import com.example.ODC_Academy.media.MediaStorageService;
import com.example.ODC_Academy.security.SecurityUtils;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.ODC_Academy.lesson.Lesson;
import com.example.ODC_Academy.lesson.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final SecurityUtils securityUtils;
    private final MediaStorageService mediaStorage;

    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository,
                         ModuleRepository moduleRepository, SecurityUtils securityUtils,
                         MediaStorageService mediaStorage) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.securityUtils = securityUtils;
        this.mediaStorage = mediaStorage;
    }

    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Transactional
    public Lesson createLesson(Long courseId, LessonRequestDTO request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cours", courseId));

        var currentUser = securityUtils.getCurrentUser();
        if (!CourseAccess.edits(currentUser, course)) {
            throw new AccessDeniedException("Seul le formateur créateur peut modifier le contenu de ce cours");
        }

        Module module = null;
        if (request.moduleId() != null) {
            module = moduleRepository.findById(request.moduleId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Module", request.moduleId()));
            if (module.getCourse() == null || !module.getCourse().getId().equals(courseId)) {
                throw new com.example.ODC_Academy.exception.BadRequestException("Le module doit appartenir au cours indiqué");
            }
        }

        Lesson lesson = Lesson.builder()
                .title(request.title())
                .content(request.content())
                .course(course)
                .module(module)
                .videoUrl(blankToNull(request.videoUrl()))
                .documentUrl(blankToNull(request.documentUrl()))
                .durationMinutes(request.durationMinutes())
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : 0)
                .build();

        return lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson createLessonWithFiles(Long courseId, LessonRequestDTO request,
                                        MultipartFile video, MultipartFile document) {
        Lesson lesson = createLesson(courseId, request);
        String videoKey = null;
        String documentKey = null;
        try {
            videoKey = mediaStorage.store(video, true);
            documentKey = mediaStorage.store(document, false);
            if (videoKey != null) lesson.setVideoUrl("/api/v1/media/" + videoKey);
            if (documentKey != null) lesson.setDocumentUrl("/api/v1/media/" + documentKey);
            return lessonRepository.save(lesson);
        } catch (RuntimeException ex) {
            if (videoKey != null) mediaStorage.delete(videoKey);
            if (documentKey != null) mediaStorage.delete(documentKey);
            throw ex;
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
