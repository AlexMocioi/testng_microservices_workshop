package com.hil.gatewayms.infra.websocket;

import java.io.IOException;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class WebsocketTestClient {

    private String uri = "wss://echo.websocket.org";
    private Session userSession = null;
    private String answer;
    private String name;

    public WebsocketTestClient(String name) {
        this.name = name;
        try {
            WebSocketContainer container = ContainerProvider
                .getWebSocketContainer();
            container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClosed() {
        return userSession == null || !userSession.isOpen();
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        this.answer = message;
        System.out.println(name + " received message: " + message);
    }

    public String getAnswer() {
        return answer;
    }

    public void sendMessage(String message) {
        if (userSession != null && userSession.isOpen())
            try {
                this.userSession.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        else {
            System.out.println("Session closed!");
        }
    }

    public void reset() {
        this.answer = null;
    }

    public static void main(String[] args) throws InterruptedException {
        WebsocketTestClient wsClient = new WebsocketTestClient("con1");

        Runnable worker1 = () -> {
            System.out.println("Sending message from Gigi...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wsClient.sendMessage("Hello from Gigi Humans in Learning @RTC2020");
        };
        worker1.run();

        Runnable worker2 = () -> {
            System.out.println("Sending message from Tanti Aglaia...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wsClient.sendMessage("Hello from Tanti Aglaia Humans in Learning @RTC2020");
        };
        worker2.run();

        Thread.sleep(100000);
    }
}
