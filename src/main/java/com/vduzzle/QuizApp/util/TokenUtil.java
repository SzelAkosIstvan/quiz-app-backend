package com.vduzzle.QuizApp.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class TokenUtil {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Titkosítási kulcs
    private static final long EXPIRATION_TIME = 864_000_000; // 10 nap (milliszekundumban)

    // Token generálása
    public static String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims) // Felhasználó adatai
                .setSubject(username) // Felhasználónév
                .setIssuedAt(new Date()) // Token kiállításának ideje
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Lejárati idő
                .signWith(SECRET_KEY) // Aláírás
                .compact();
    }

    // Token ellenőrzése és adatok kinyerése
    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}