package com.vduzzle.QuizApp.quiz;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {
    @JsonProperty("type")
    private String type = "closed";

    @JsonProperty("question")
    private String questionText;

    @JsonProperty("possibleAns")
    private List<String> possibleAnswers = new ArrayList<>();

    @JsonProperty("correctAnswer")
    private String correctAnswer = "";

    // Constructors
    public Question() {
        this.type = "closed";
        this.questionText = "";
        this.possibleAnswers = new ArrayList<>();
        this.correctAnswer = "";
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getPossibleAnswers() {
        return possibleAnswers;
    }

    public void setPossibleAnswers(List<String> possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String toString() {
        return "Question{" +
                "type='" + type + '\'' +
                ", questionText='" + questionText + '\'' +
                ", possibleAnswers=" + possibleAnswers +
                ", correctAnswer='" + correctAnswer + '\'' +
                '}';
    }
}