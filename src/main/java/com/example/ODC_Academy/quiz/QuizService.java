package com.example.ODC_Academy.quiz;

import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.exception.ResourceNotFoundException;
import com.example.ODC_Academy.lesson.Lesson;
import com.example.ODC_Academy.lesson.LessonRepository;
import com.example.ODC_Academy.model.quiz.*;
import com.example.ODC_Academy.progress.LessonProgressService;
import com.example.ODC_Academy.quiz.QuizDtos.*;
import com.example.ODC_Academy.security.CourseAccess;
import com.example.ODC_Academy.security.CourseContentAccess;
import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {
    private final QuizRepository quizzes;
    private final QuizAttemptRepository attempts;
    private final LessonRepository lessons;
    private final LessonProgressService lessonProgress;
    private final CourseContentAccess contentAccess;

    public QuizService(QuizRepository q, QuizAttemptRepository a, LessonRepository l, LessonProgressService p,
                       CourseContentAccess contentAccess) {
        this.quizzes = q; this.attempts = a; this.lessons = l; this.lessonProgress = p;
        this.contentAccess = contentAccess;
    }

    private Lesson lesson(Long id) {
        return lessons.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Leçon", id));
    }

    /** Création ou remplacement du quiz d'une leçon (un quiz par leçon). */
    public QuizView save(Long lessonId, QuizRequest r, User me) {
        Lesson lesson = lesson(lessonId);
        if (!CourseAccess.edits(me, lesson.getCourse())) throw new AccessDeniedException("Seul le formateur créateur peut modifier le contenu de ce cours");
        for (QuestionRequest q : r.questions()) {
            long ok = q.options().stream().filter(OptionRequest::correct).count();
            if (q.options().size() < 2) throw new BadRequestException("Chaque question exige au moins 2 réponses");
            if (ok == 0) throw new BadRequestException("Chaque question exige une bonne réponse : « " + q.prompt() + " »");
            if (q.type() != QuestionType.MULTIPLE_CHOICE && ok != 1)
                throw new BadRequestException("Une seule bonne réponse pour ce type : « " + q.prompt() + " »");
        }
        Quiz quiz = quizzes.findByLessonId(lessonId).orElseGet(() -> Quiz.builder().lesson(lesson).build());
        quiz.setTitle(r.title().trim());
        quiz.setDescription(r.description());
        quiz.setPassingScore(r.passingScore());
        quiz.getQuestions().clear(); // orphanRemoval
        for (QuestionRequest qr : r.questions()) {
            Question q = Question.builder().prompt(qr.prompt().trim()).type(qr.type()).quiz(quiz).build();
            for (OptionRequest o : qr.options())
                q.getOptions().add(AnswerOption.builder().label(o.label().trim()).correct(o.correct()).question(q).build());
            quiz.getQuestions().add(q);
        }
        return view(quizzes.save(quiz), true);
    }

    @Transactional(readOnly = true)
    public Optional<QuizView> forLesson(Long lessonId, User me) {
        Lesson lesson = lesson(lessonId);
        contentAccess.assertCanView(me, lesson.getCourse());
        return quizzes.findByLessonId(lessonId).map(q -> view(q, CourseAccess.manages(me, lesson.getCourse())));
    }

    private QuizView view(Quiz q, boolean withAnswers) {
        return new QuizView(q.getId(), q.getLesson().getId(), q.getTitle(), q.getDescription(), q.getPassingScore(),
                q.getQuestions().stream().map(qu -> new QuestionView(qu.getId(), qu.getPrompt(), qu.getType(),
                        qu.getOptions().stream().map(o -> new OptionView(o.getId(), o.getLabel(), withAnswers ? o.isCorrect() : null)).toList())).toList());
    }

    /** Correction automatique : une question est juste si l'ensemble des réponses cochées = l'ensemble des bonnes réponses. */
    public AttemptResult attempt(Long quizId, AttemptRequest r, User me) {
        if (me.getRole() != Role.APPRENANT) throw new AccessDeniedException("Réservé aux apprenants");
        Quiz quiz = quizzes.findById(quizId).orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));
        contentAccess.assertCanView(me, quiz.getLesson().getCourse());
        List<Long> wrong = new ArrayList<>();
        int ok = 0;
        for (Question q : quiz.getQuestions()) {
            Set<Long> good = q.getOptions().stream().filter(AnswerOption::isCorrect).map(AnswerOption::getId).collect(Collectors.toSet());
            Set<Long> given = new HashSet<>(r.answers().getOrDefault(q.getId(), List.of()));
            if (good.equals(given)) ok++; else wrong.add(q.getId());
        }
        int total = quiz.getQuestions().size();
        int score = total == 0 ? 0 : (int) Math.round(100.0 * ok / total);
        boolean passed = score >= quiz.getPassingScore();
        attempts.save(QuizAttempt.builder().user(me).quiz(quiz).score(score).passed(passed).build());
        if (passed) lessonProgress.completeInternal(quiz.getLesson(), me); // quiz réussi = leçon terminée
        return new AttemptResult(score, passed, quiz.getPassingScore(), ok, total, wrong);
    }
}
