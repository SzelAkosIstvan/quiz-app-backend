package com.vduzzle.QuizApp.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vduzzle.QuizApp.util.TokenUtil;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class userController {
    private User user;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserRequest userRequest) {
        String id = userRequest.getId();

        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("ID cannot be null or empty");
        }

        try {
            user = new User(id);
        } catch (User.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        String token;
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", id);
            claims.put("username", user.getUsername());
            claims.put("teacherRole", user.getIsTeacher());
            token = TokenUtil.generateToken(id, claims);//change
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token generation failed");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "You are logged in successfully.");
        response.put("token", token);
        response.put("username", user.getUsername());//not needed anymore
        response.put("isTeacher", user.getIsTeacher().toString());//not needed anymore
        response.put("image", user.getImage());//not needed anymore

        return ResponseEntity.ok(response);
    }

    @PostMapping("/setAvatar")
    public ResponseEntity<String> avatarSetting(@RequestHeader("Authorization") String authHeader, @RequestBody String newAvatar)
    {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing token.");
            }

            String token = authHeader.substring(7);
            Claims claims = TokenUtil.extractClaims(token);
            String updatedUserID = (String) claims.get("id");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> jsonMap = objectMapper.readValue(newAvatar, Map.class);
            String newImage = jsonMap.get("newImage");

            user.setImage(updatedUserID, newImage);

            return ResponseEntity.ok("Profilkép sikeresen frissítve.");
        }  catch (Exception e) {
            return ResponseEntity.status(500).body("Hiba történt a profilkép frissítése során.");
        }

    }

    @Getter
    static class UserRequest {
        private String id;
    }
}
