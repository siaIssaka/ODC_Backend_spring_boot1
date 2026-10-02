package com.example.ODC_Academy.assignment;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssignmentTest {
    @Test
    void closesSubmissionAtTheExactDeadline() {
        LocalDateTime opensAt = LocalDateTime.parse("2026-10-01T09:00:00");
        LocalDateTime closesAt = LocalDateTime.parse("2026-10-01T17:00:00");
        Assignment assignment = Assignment.builder().opensAt(opensAt).closesAt(closesAt).build();

        assertEquals("PLANIFIE", assignment.status(opensAt.minusSeconds(1)));
        assertEquals("OUVERT", assignment.status(opensAt));
        assertEquals("OUVERT", assignment.status(closesAt.minusSeconds(1)));
        assertEquals("FERME", assignment.status(closesAt));
    }
}
