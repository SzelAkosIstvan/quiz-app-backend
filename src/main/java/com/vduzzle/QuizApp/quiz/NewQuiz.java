package com.vduzzle.QuizApp.quiz;

import java.util.List;

public class NewQuiz {
    private String title;
    private List<Question> questions;

    public NewQuiz() {}

    public NewQuiz(String title, List<Question> questions) {
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}