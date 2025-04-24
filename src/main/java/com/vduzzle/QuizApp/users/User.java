package com.vduzzle.QuizApp.users;

import com.vduzzle.QuizApp.config.DatabaseConfig;
import lombok.Getter;

import java.sql.*;

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


    static class UserNotFoundException extends Exception {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
