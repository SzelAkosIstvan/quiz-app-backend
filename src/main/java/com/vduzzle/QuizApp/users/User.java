package com.vduzzle.QuizApp.users;

import com.vduzzle.QuizApp.config.DatabaseConfig;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userID;
    @Getter
    private String username;
    @Getter
    private String image;
    @Getter
    private Boolean isTeacher;

    public User(String id) throws UserNotFoundException{
        try (Connection conn = DatabaseConfig.getStaticDataSource().getConnection()) {
            String query = "SELECT username, image, isTeacher FROM users WHERE id::varchar = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, id.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        this.userID = id;
                        this.username = rs.getString("username");
                        this.image = rs.getString("image");
                        this.isTeacher = rs.getBoolean("isTeacher");
                    } else {
                        throw new UserNotFoundException("User does not exist with ID: " + id);
                    }
                } catch (SQLException | UserNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void setImage(String updatedUserID, String image) {
        if (updatedUserID == null || updatedUserID.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        this.image = image.trim();
        try (Connection conn = DatabaseConfig.getStaticDataSource().getConnection()) {
            String query = "UPDATE users SET image = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, image.trim());
                stmt.setString(2, updatedUserID);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new RuntimeException("No user found with ID: " + updatedUserID);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public List<Map<String, Integer>> getQuizList(String userID) {
        if (userID == null || userID.trim().isEmpty()) {
            System.out.println("User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        //List<String> quizList = new ArrayList<>();
        List<Map<String, Integer>> quizList = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getStaticDataSource().getConnection()) {
            String query = "SELECT quizname, quizid FROM quizzes WHERE createdby = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        //quizList.add(rs.getString("quizname"));
                        Map<String, Integer> quiz = new HashMap<>();
                        quiz.put(rs.getString("quizname"), rs.getInt("quizid"));
                        quizList.add(quiz);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred");
            throw new RuntimeException("Database error occurred", e);
        }
        return quizList;
    }

    public void deleteQuiz(String userID,String quizID) {
        if (userID == null || userID.trim().isEmpty()) {
            System.out.println("User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        try (Connection conn = DatabaseConfig.getStaticDataSource().getConnection()) {
            String deleteAnswersQuery = "DELETE FROM answers WHERE answergroupid IN " +
                    "(SELECT answergroupid FROM questions WHERE quizid = ?)";
            String deleteQuestionsQuery = "DELETE FROM questions WHERE quizid = ?";
            String deleteQuizQuery = "DELETE FROM quizzes WHERE createdby = ? AND quizid = ?";
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(deleteAnswersQuery)) {
                    stmt.setInt(1, Integer.parseInt(quizID));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuestionsQuery)) {
                    stmt.setInt(1, Integer.parseInt(quizID));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuizQuery)) {
                    stmt.setString(1, userID);
                    stmt.setInt(2, Integer.parseInt(quizID));
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new RuntimeException("No quiz found with the given ID for this user");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred");
            throw new RuntimeException("Database error occurred", e);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Quiz ID format");
            throw new IllegalArgumentException("Quiz ID must be a number", e);
        }
    }

    static class UserNotFoundException extends Exception {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
