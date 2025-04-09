package com.vduzzle.QuizApp.quiz;

import java.util.Random;

public class QuizCodeGenerator {
    public static String generateQuizCode() {
        Random random = new Random();
        int code = random.nextInt(90000000) + 10000000;
        return String.valueOf(code);
    }
}