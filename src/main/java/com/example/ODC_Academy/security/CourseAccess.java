package com.example.ODC_Academy.security;

import com.example.ODC_Academy.course.Course;
import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;

/** Règles d'accès communes (comparaison par id : les entités viennent de sessions JPA différentes). */
public final class CourseAccess {
    private CourseAccess() { }

    public static boolean trains(User u, Formation f) {
        return u.getRole() == Role.FORMATEUR && f != null
                && f.getTrainers().stream().anyMatch(t -> t.getId().equals(u.getId()));
    }

    /** ADMIN, ou formateur affecté à la formation du cours. */
    public static boolean manages(User u, Course c) {
        return u.getRole() == Role.ADMIN || trains(u, c.getFormation());
    }

    /** Créateur only for modern courses; an assigned trainer can claim legacy courses without an owner. */
    public static boolean edits(User u, Course c) {
        if (u.getRole() != Role.FORMATEUR || c == null) return false;
        if (c.getCreatedBy() != null) return c.getCreatedBy().getId().equals(u.getId());
        return trains(u, c.getFormation());
    }
}
