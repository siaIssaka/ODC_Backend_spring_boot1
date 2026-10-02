package com.example.ODC_Academy.course;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseDTO toDto(Course course) {
        Long formationId = course.getFormation() != null ? course.getFormation().getId() : null;
        return new CourseDTO(
                course.getId(),
                formationId,
                course.getTitle(),
                course.getDescription(),
                course.getLevel(),
                course.getPrice(),
                course.isActive(),
                course.getCreatedAt(),
                course.getCreatedBy() == null ? null : course.getCreatedBy().getId(),
                course.getCreatedBy() == null ? null :
                        course.getCreatedBy().getPrenom() + " " + course.getCreatedBy().getNom()
        );
    }
}
