package com.vduzzle.QuizApp.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.vduzzle.QuizApp.dbo.Question;

import java.util.List;

@Controller
public class WebSocketController {

    @Autowired
    private QuizManager quizManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tanár indít egy új kvízt.
     */
    @MessageMapping("/start-quiz")
    public void startQuiz(QuizStartRequest request) {
        String teacherId = request.getTeacherId();
        String quizId = request.getQuizId();

        // Kvíz kód generálása
        String quizCode = QuizCodeGenerator.generateQuizCode();

        // Kvíz kód küldése a tanárnak
        messagingTemplate.convertAndSend("/topic/teacher/" + teacherId + "/quiz-code", quizCode);
        QuizManager.startNewQuiz(quizCode, quizId, teacherId);
    }

    /**
     * Diák csatlakozása a kvízhez.
     */
    @MessageMapping("/join-quiz")
    public void joinQuiz(String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null && session.isActive()) {
            messagingTemplate.convertAndSend("/topic/quiz/" + quizCode, "Wait for other people to join");
            //List<QuizManager.Question> questions = quizManager.getQuestionsForQuiz(session.getQuizId());
            //messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/questions", questions);
        } else {
            messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/error", "Érvénytelen kvíz kód.");
        }
    }

    /**
     * Válasz küldése a kvízre.   ez szemet
     */
    @MessageMapping("/submit-answer")
    public void submitAnswer(@Payload String payload) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(payload);
        String quizCode = json.get("fullQuizCode").asText();
        String answer = json.get("answer").asText();
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {
            System.out.println("new submission:"+ answer);
            messagingTemplate.convertAndSend("/topic/teacher/" + session.getTeacherId() + "/answers", answer);
        }
    }

    @MessageMapping("/next-question")
    public void nextQuestion(@Payload String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {
            String[] currentQuestion = {"Test question hardcoded, if you see this, something is wrong", "Masodik kerdes", "Harmadik kerdes megint hosszu, hogy jol lehessen tesztelni", "negyedik teszt kerdes kozepes", "Otodik eddig kimaradt" ,"utolso csumi csumi"};
            String[][] possibleAnswers = {{"egy", "egy", "hat", "het"}, {"masodik k", "valasz 2", "valasz 3", "valasz 5"}, {""}, {"a", "b", "c", "d"}, {"hehe", "alma", "citrom", "korte"}, {"qwe", "asd", "kjg", "asd"}};
            String[] imgLinks = {"https://picsum.photos/600/400", "https://picsum.photos/602/402", "https://picsum.photos/604/404", "https://picsum.photos/600/400", "", ""};
            int currentIndex = quizManager.getNextQuestionID(quizCode);
            if(currentIndex>currentQuestion.length-1) {
                quizManager.endQuiz(quizCode);
                messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/end", "");
            } else {
                quizManager.setNextQuestionID(quizCode);
                Question questionData = new Question(
                        currentQuestion[currentIndex],
                        possibleAnswers[currentIndex],
                        imgLinks[currentIndex]
                );
                messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/question" , questionData);
                //quizManager.stepWithTestID();
            }
        }
    }

    @MessageMapping("/current-correct")
    public void correctAnswer(String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {
            String[] correctAnswers = {"het", "masodik k", "23", "c", "alma", "asd"};
            int currentIndex = quizManager.getNextQuestionID(quizCode);
            System.out.println("jelenlegi kerdesre a helyes valasz: "+correctAnswers[currentIndex]);
            messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/answers", correctAnswers[currentIndex]);
            if(quizManager.getNextQuestionID(quizCode)<=correctAnswers.length) {
//                quizManager.setNextQuestionID(quizCode);
            } else {
                quizManager.endQuiz(quizCode);
                messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/end", "");
            }
        }
    }

    @MessageMapping("/request-question")
    public void reqQuestion(String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {
            //String currentQuestion = "Test question hardcoded, if you see this, something is wrong";
            //messagingTemplate.convertAndSend("/topic/quiz/" + quizCode + "/question" , currentQuestion);
        }
    }

    @MessageMapping("/give-answer")
    public void giveAnswer(String quizCode, String answer) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {

        }
    }

    @MessageMapping("/paused")
    public void pause(String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {//itt keress szebb modszert if az ifben nem tetszik
            if(session.isPaused())
                session.resume();//not protected or private
            else
                session.pause();//vulnerability
        }
    }

    @MessageMapping("/status")
    public void status(String quizCode) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {

        }
    }

    @MessageMapping("/result")
    public void result(String quizCode, String answer) {
        QuizManager.QuizSession session = quizManager.getQuizSession(quizCode);
        if (session != null) {

        }
    }

    public static class QuizStartRequest {
        private String quizId;
        private String teacherId;

        // Getter és setter metódusok
        public String getQuizId() {
            return quizId;
        }

        public void setQuizId(String quizId) {
            this.quizId = quizId;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(String teacherId) {
            this.teacherId = teacherId;
        }
    }

}