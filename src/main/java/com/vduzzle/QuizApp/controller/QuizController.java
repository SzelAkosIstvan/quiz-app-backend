package com.vduzzle.QuizApp.controller;

import com.vduzzle.QuizApp.config.DatabaseConfig;
import com.vduzzle.QuizApp.quiz.NewQuiz;
import com.vduzzle.QuizApp.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vduzzle.QuizApp.quiz.Question;

import java.sql.*;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @PostMapping
    public ResponseEntity<?> createQuiz(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody NewQuiz quizRequest) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid authorization token");
        }

        String token = authHeader.substring(7);
        Claims claims = TokenUtil.extractClaims(token);
        String UserID = (String) claims.get("id");

        try {
            // Validation for quiz data
            if (quizRequest.getTitle() == null || quizRequest.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Quiz title cannot be empty");
            }

            if (quizRequest.getQuestions() == null || quizRequest.getQuestions().isEmpty()) {
                return ResponseEntity.badRequest().body("Quiz must contain at least one question");
            }

            // Enhanced question validation
            for (Question question : quizRequest.getQuestions()) {
                // Modify validation for open questions
                if (question.getType().equals("closed")) {
                    if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message","Closed questions must have a non-empty question text"));
                    }

                    if (question.getPossibleAnswers() == null || question.getPossibleAnswers().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message","Closed questions must have possible answers"));
                    }

                    if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message","Closed questions must have a correct answer"));
                    }
                } else if (question.getType().equals("open")) {
                    if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
                        return ResponseEntity.badRequest().body("Open questions must have a non-empty question text");
                    }

                    if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
                        return ResponseEntity.badRequest().body("Open questions must have a correct answer");
                    }
                }
            }
            int quizId = saveQuizToDatabase(quizRequest, UserID);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Quiz created successfully",
                    "quizId", quizId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating quiz: " + e.getMessage());
        }
    }

    private int saveQuizToDatabase(NewQuiz quiz, String userId) throws SQLException {
        try (Connection conn = DatabaseConfig.getStaticDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int quizId = getNextQuizId(conn);
            try {
                String quizQuery = "INSERT INTO quizzes (QuizName, CreatedBy) VALUES (?, ?)";
                try (PreparedStatement quizStmt = conn.prepareStatement(quizQuery, Statement.RETURN_GENERATED_KEYS)) {
                    quizStmt.setString(1, quiz.getTitle());
                    quizStmt.setString(2, userId);
                    quizStmt.executeUpdate();

                    try (ResultSet generatedKeys = quizStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            quizId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating quiz failed, no ID obtained.");
                        }
                    }
                }

                String questionQuery = "INSERT INTO questions (QuizID, Question, AnswerGroupID, ImageKey) VALUES (?, ?, ?, ?)";
                String answerQuery = "INSERT INTO answers (AnswerGroupID, Answer, Correct) VALUES (?, ?, ?)";
                int questionId = 1;
                for (Question question : quiz.getQuestions()) {

                    String answerGroupId = "q"+quizId+"q"+questionId+userId;
                    // Insert question
                    try (PreparedStatement questionStmt = conn.prepareStatement(questionQuery)) {
                        questionStmt.setInt(1, quizId);
                        questionStmt.setString(2, question.getQuestionText());
                        questionStmt.setString(3, answerGroupId);
                        questionStmt.setString(4, question.getImageKey());
                        questionStmt.executeUpdate();
                    }

                    // Insert possible answers (for closed questions)
                    if (question.getPossibleAnswers() != null && !question.getPossibleAnswers().isEmpty()) {
                        for (String answer : question.getPossibleAnswers()) {
                            try (PreparedStatement answerStmt = conn.prepareStatement(answerQuery)) {
                                answerStmt.setString(1, answerGroupId);
                                answerStmt.setString(2, answer);
                                answerStmt.setBoolean(3, answer.equals(question.getCorrectAnswer()));
                                answerStmt.executeUpdate();
                            }
                        }
                    }
                    questionId++;
                }

                conn.commit();
                return quizId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int getNextQuizId(Connection conn) throws SQLException {
        String query = "SELECT COALESCE(MAX(QuizID), 0) + 1 AS nextId FROM quizzes";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
            return 1; // If table is empty
        }
    }
}