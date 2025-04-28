package com.vduzzle.QuizApp.quiz;

import java.sql.*;
import java.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QuizManager {

    private static Map<String, QuizSession> activeQuizzes = new HashMap<>();
    private static Connection connection;

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

    public static void startNewQuiz(String quizCode, String quizId, String teacherId) {
        activeQuizzes.put(quizCode, new QuizSession(quizId, teacherId));
        //itt hozd letre a kerdes es valaszokat
    }

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

    public void endQuiz(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            session.end();
            activeQuizzes.remove(quizCode);
        }
    }

    public QuizSession getQuizSession(String quizCode) {
        return activeQuizzes.get(quizCode);
    }

    public String[] getQuizQuestions(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            return session.getQuizquestions().toArray(new String[0]);
        }
        return new String[0];
    }

    public String[][] getQuizAnswers(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            List<List<String>> answers = session.getQuizanswers();
            String[][] result = new String[answers.size()][];
            for (int i = 0; i < answers.size(); i++) {
                List<String> innerList = answers.get(i);
                result[i] = innerList.toArray(new String[0]);
            }
            return result;
        }
        return new String[0][];
    }

    public String[] getCorrectAnswers(String quizCode) {
        QuizSession session = activeQuizzes.get(quizCode);
        if (session != null) {
            System.out.println(Arrays.toString(session.getQuizcorrectanswers().toArray(new String[0])));
            return session.getQuizcorrectanswers().toArray(new String[0]);
        }
        return new String[0];
    }

    public static class QuizSession {
        private String quizId;
        private String teacherId;
        private boolean isActive = true;
        private boolean isPaused = false;
        private List<String> quizquestions;
        private List<List<String>> quizanswers;
        private List<String> quizcorrectanswers;
        @Getter
        private int currentQuestionIndex = 0;

        public void NextQuestionIndex() {
            currentQuestionIndex++;
        }

        public QuizSession(String quizId, String teacherId) {
            this.quizId = quizId;
            this.teacherId = teacherId;
            this.quizquestions = new ArrayList<>();
            this.quizanswers = new ArrayList<>();
            this.quizcorrectanswers = new ArrayList<>();
            String answerGroupId = "";
            String query2;
            String query = "SELECT Question, AnswerGroupID FROM questions WHERE QuizID::varchar = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, quizId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String questionText = rs.getString("Question");
                    answerGroupId = rs.getString("AnswerGroupID");
                    quizquestions.add(questionText);

                    query2 = "SELECT Answer, Correct FROM answers WHERE AnswerGroupID = ?";
                    try (PreparedStatement stmt2 = connection.prepareStatement(query2)) {
                        stmt2.setString(1, answerGroupId);
                        ResultSet rs2 = stmt2.executeQuery();
                        List<String> currentAnswers = new ArrayList<>();
                        while (rs2.next()) {
                            String answerText = rs2.getString("Answer");
                            boolean isCorrectAns = rs2.getBoolean("Correct");
                            currentAnswers.add(answerText);
                            if (isCorrectAns) {
                                quizcorrectanswers.add(answerText);
                            }
                        }
                        quizanswers.add(currentAnswers);
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to fetch questions", e);
                    }

                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch questions", e);
            }
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

        public List<String> getQuizquestions() {
            return quizquestions;
        }
        public List<List<String>> getQuizanswers() {
            return quizanswers;
        }
        public List<String> getQuizcorrectanswers() {
            return quizcorrectanswers;
        }
    }
}