package com.example.ODC_Academy.security;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseAccessTest {
    @Test
    void onlyCourseCreatorCanEditAndAdminRemainsReadOnly() {
        User creator = User.builder().id(10L).role(Role.FORMATEUR).build();
        User otherTrainer = User.builder().id(11L).role(Role.FORMATEUR).build();
        User admin = User.builder().id(12L).role(Role.ADMIN).build();
        Course course = Course.builder().createdBy(creator).build();

        assertTrue(CourseAccess.edits(creator, course));
        assertFalse(CourseAccess.edits(otherTrainer, course));
        assertFalse(CourseAccess.edits(admin, course));
    }

    @Test
    void assignedTrainerCanClaimAnOlderCourseWithoutRecordedCreator() {
        User assignedTrainer = User.builder().id(10L).role(Role.FORMATEUR).build();
        User otherTrainer = User.builder().id(11L).role(Role.FORMATEUR).build();
        Formation formation = Formation.builder().trainers(java.util.Set.of(assignedTrainer)).build();
        Course legacyCourse = Course.builder().formation(formation).createdBy(null).build();

        assertTrue(CourseAccess.edits(assignedTrainer, legacyCourse));
        assertFalse(CourseAccess.edits(otherTrainer, legacyCourse));
    }
}
