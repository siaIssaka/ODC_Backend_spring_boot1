ALTER TABLE course_sessions
    ADD COLUMN formation_id bigint REFERENCES formations (id) ON DELETE CASCADE;

UPDATE course_sessions session
SET formation_id = course.formation_id
FROM courses course
WHERE session.course_id = course.id;

CREATE TABLE course_session_courses (
    session_id bigint NOT NULL REFERENCES course_sessions (id) ON DELETE CASCADE,
    course_id  bigint NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    PRIMARY KEY (session_id, course_id)
);

INSERT INTO course_session_courses (session_id, course_id)
SELECT id, course_id
FROM course_sessions
WHERE course_id IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE course_sessions DROP COLUMN course_id;

DROP INDEX IF EXISTS idx_course_sessions_course_dates;
CREATE INDEX idx_course_sessions_formation_dates
    ON course_sessions (formation_id, starts_at, ends_at);

DROP INDEX IF EXISTS uq_enrollments_user_session;
CREATE UNIQUE INDEX uq_enrollments_user_session_course
    ON enrollments (user_id, session_id, course_id)
    WHERE session_id IS NOT NULL;
