package com.vduzzle.QuizApp.quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QuizManager {

    private static Map<String, QuizSession> activeQuizzes = new HashMap<>();
    private Connection connection;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void init() {
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // log error
            }
        }
    }

    public void setNextQuestionID(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            session.NextQuestionIndex();
        }
    }

    public int getNextQuestionID(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            return session.getCurrentQuestionIndex();
        }
        return 0;
    }

    /**
     * Új kvíz indítása.
     */
    public static void startNewQuiz(String quizCode, String quizId, String teacherId) {
        activeQuizzes.put(quizCode, new QuizSession(quizId, teacherId));
        //itt hozd letre a kerdes es valaszokat
    }

    /**
     * Kvíz szüneteltetése.
     */
    public void pauseQuiz(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            session.pause();
        }
    }

    //when teacher hits [space] for the first time it is used 100!!4!
    public void resumeQuiz(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            session.resume();
        }
    }

    /**
     * Kvíz befejezése.
     */
    public void endQuiz(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            session.end();
            activeQuizzes.remove(quizCode);
        }
    }

    /**
     * Kvíz állapotának lekérése.
     */
    public QuizSession getQuizSession(String quizCode) {
        return activeQuizzes.get(quizCode);
    }

    /**
     * Kérdések lekérése egy kvízhez.
     */
    public List<Question> getQuestionsForQuiz(String quizId) {
        List<Question> questions = new ArrayList<>();

        String query = "SELECT QuestionID, Question, AnswerGroupID FROM questions WHERE QuizID::varchar = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, quizId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String questionId = rs.getString("QuestionID");
                String questionText = rs.getString("Question");
                String answerGroupId = rs.getString("AnswerGroupID");

                // Lekéri a válaszokat is
                List<Answer> answers = getAnswersForQuestion(answerGroupId);
                questions.add(new Question(questionId, questionText, answers));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch questions", e);
        }

        return questions;
    }

    public Question getQuestion(String quizCode, String questionId) {
        //valahogy vizsgalni kell, hogy a quizCodehoz tartozik e es hanyadik
        return null;
    }

    /**
     * Válaszok lekérése egy kérdéshez.
     */
    private List<Answer> getAnswersForQuestion(String answerGroupId) {
        List<Answer> answers = new ArrayList<>();

        String query = "SELECT Answer, Correct FROM answers WHERE AnswerGroupID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, answerGroupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String answerText = rs.getString("Answer");
                boolean isCorrect = rs.getBoolean("Correct");
                answers.add(new Answer(answerText, isCorrect));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch answers", e);
        }

        return answers;
    }

    /**
     * Kvíz session osztály.
     */
    public static class QuizSession {
        private String quizId;
        private String teacherId;
        private boolean isActive = true;
        private boolean isPaused = false;
        private List<Question> questions;
        @Getter
        private int currentQuestionIndex = 0;

        public void NextQuestionIndex() {
            currentQuestionIndex++;
        }

        public QuizSession(String quizId, String teacherId) {
            this.quizId = quizId;
            this.teacherId = teacherId;
            this.questions = new ArrayList<>();
        }

        public void pause() {
            isPaused = true;
        }

        public void resume() {
            isPaused = false;
        }

        public void end() {
            isActive = false;
        }

        public boolean isActive() {
            return isActive;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public String getQuizId() {
            return quizId;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }

    /**
     * Kérdés osztály.
     */
    public static class Question {
        private String questionId;
        private String questionText;
        private List<Answer> answers;

        public Question(String questionId, String questionText, List<Answer> answers) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.answers = answers;
        }

        public String getQuestionId() {
            return questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public List<Answer> getAnswers() {
            return answers;
        }
    }

    /**
     * Válasz osztály.
     */
    public static class Answer {
        private String answerText;
        private boolean isCorrect;

        public Answer(String answerText, boolean isCorrect) {
            this.answerText = answerText;
            this.isCorrect = isCorrect;
        }

        public String getAnswerText() {
            return answerText;
        }

        public boolean isCorrect() {
            return isCorrect;
        }
    }
}