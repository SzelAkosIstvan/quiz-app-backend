package com.vduzzle.QuizApp.dbo;

public class Question {
    private String questionText;
    private String[] possibleAnswers;
    private String imageLink;

    // Constructors, getters, and setters
    public Question(String questionText, String[] possibleAnswers, String imageLink) {
        this.questionText = questionText;
        this.possibleAnswers = possibleAnswers;
        this.imageLink = imageLink;
    }

    // Getters and setters
    public String getQuestionText() { return questionText; }
    public String[] getPossibleAnswers() { return possibleAnswers; }
    public String getImageLink() { return imageLink; }
}