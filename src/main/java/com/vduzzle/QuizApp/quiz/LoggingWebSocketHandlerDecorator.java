package com.vduzzle.QuizApp.quiz;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class LoggingWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    public LoggingWebSocketHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Kapcsolat létrejött: " + session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("Kapcsolat lezárva: " + session.getId() + ", ok: " + closeStatus);
        super.afterConnectionClosed(session, closeStatus);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Hiba történt: " + exception.getMessage());
        super.handleTransportError(session, exception);
    }
}