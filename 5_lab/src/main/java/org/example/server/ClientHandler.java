package org.example.server;

import org.example.auth.UserStore;
import org.example.protocol.Message;
import org.example.protocol.Transport;
import org.example.protocol.XmlTransport;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


public class ClientHandler implements Runnable {

    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());

    private final Socket    socket;
    private final ChatRoom  room;
    private final UserStore users;

    private Transport transport;
    private String    username  = "";
    private String    sessionId = "";
    private boolean   loggedIn  = false;


    public ClientHandler(Socket socket, ChatRoom room, UserStore users) {
        this.socket = socket;
        this.room   = room;
        this.users  = users;
    }

    @Override
    public void run() {
        try {
            transport = new XmlTransport(socket);
            handleLogin();
            if (loggedIn) {
                sendHistory();
                room.join(this);
                messageLoop();
            }
        } catch (IOException e) {
            log.fine("Connection error for " + username + ": " + e.getMessage());
        } finally {
            room.leave(this);
            closeQuietly();
        }
    }



    private void handleLogin() throws IOException {
        Message msg = transport.receive();
        if (msg == null || msg.getType() != Message.Type.LOGIN) {
            sendError("Expected login command");
            return;
        }

        String nick = msg.getSender();
        String pass = msg.getSession();

        if (nick.isBlank()) {
            sendError("Username cannot be empty");
            return;
        }

        // auth: регистрируем если нет, проверяем если есть
        if (!users.exists(nick)) {
            if (pass.isBlank()) {
                sendError("New user requires a password");
                return;
            }
            users.register(nick, pass);
            log.info("Registered new user: " + nick);
        } else {
            if (!users.authenticate(nick, pass)) {
                sendError("Wrong password");
                return;
            }
        }

        this.username  = nick;
        this.sessionId = UUID.randomUUID().toString();
        this.loggedIn  = true;


        transport.send(new Message(Message.Type.SUCCESS, "", sessionId, sessionId, 0));
        log.info("User logged in: " + nick + " (" + sessionId + ")");
    }


    private void sendHistory() throws IOException {
        for (Message m : room.recentHistory()) {

            Message evt = new Message(
                    Message.Type.EVENT_MESSAGE,
                    m.getSender(), m.getBody(), null, m.getSeqNum()
            );
            transport.send(evt);
        }
    }

    private void messageLoop() throws IOException {
        while (!socket.isClosed()) {
            Message msg;
            try {
                msg = transport.receive();
            } catch (SocketTimeoutException e) {
                log.info("Timeout for " + username + ", disconnecting");
                break;
            }

            if (msg == null) break;

            if (!sessionId.equals(msg.getSession())) {
                sendError("Invalid session");
                continue;
            }

            switch (msg.getType()) {
                case CHAT   -> handleChat(msg);
                case LIST_REQUEST   -> handleList();
                case LOGOUT -> { return; }
                default     -> sendError("Unknown command");
            }
        }
    }

    private void handleChat(Message msg) throws IOException {
        if (msg.getBody().isBlank()) {
            sendError("Empty message");
            return;
        }
        Message chatMsg = new Message(
                Message.Type.EVENT_MESSAGE,
                username, msg.getBody(), null, 0
        );
        room.publish(chatMsg);
        transport.send(new Message(Message.Type.SUCCESS, "", "", sessionId, 0));
    }

    private void handleList() throws IOException {
        String names = room.getOnlineList();
        transport.send(new Message(Message.Type.LIST_RESPONSE, "", names, sessionId, 0));
    }



    public synchronized void sendMessage(Message msg) throws IOException {
        transport.send(msg);
    }

    private void sendError(String reason) throws IOException {
        transport.send(Message.of(Message.Type.ERROR, "", reason));
    }

    private void closeQuietly() {
        try { transport.close(); } catch (Exception ignored) {}
    }

    public String getUsername() { return username; }

    public String getSessionId() { return sessionId; }
}