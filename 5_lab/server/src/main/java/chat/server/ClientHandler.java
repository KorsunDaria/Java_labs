package chat.server;

import chat.protocol.*;
import chat.protocol.message.*;

import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private static final int TIMEOUT_MS = 300_000;

    private final Socket       socket;
    private final ChatRoom     room;
    private final UserStore    users;
    private final ServerLogger logger;

    private Protocol protocol;
    private String   username;
    private String   session;

    public ClientHandler(Socket socket, ChatRoom room, UserStore users, ServerLogger logger) {
        this.socket = socket;
        this.room   = room;
        this.users  = users;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(TIMEOUT_MS);
            protocol = Server.detectProtocol(socket); // доп класс
            handleMessages();

        } catch (IOException e) {
            logger.log(" disconnected (timeout): " +
                    (username != null ? username : socket.getRemoteSocketAddress()));
        } finally {
            cleanup();
        }
    }

    private void handleMessages() throws IOException {
        while (true) {
            Message msg = protocol.readMessage();
            logger.log("Take [" + (username != null ? username : "?") + "]: " + msg);

            if (msg instanceof LoginMsg m)        { handleLogin(m); }
            else if (msg instanceof LogoutMsg)    { handleLogout(); return; }
            else if (msg instanceof SendMessageMsg m) { handleMessage(m); }
            else if (msg instanceof ListRequestMsg m) { handleList(m); }
            else { sendError("Неизвестный тип сообщения"); }
        }
    }

    private void handleLogin(LoginMsg msg) throws IOException {
        if (msg.getName() == null || msg.getName().isBlank()) {
            sendError("Name could be not empty");
            return;
        }

        String error = users.authenticate(msg.getName(), msg.getPassword());
        if (error != null) {
            sendError(error);
            return;
        }

        if (room.isOnline(msg.getName())) {
            sendError("Dubliccate name");
            return;
        }

        this.username = msg.getName();
        this.session  = java.util.UUID.randomUUID().toString();

        room.join(this);
        protocol.writeMessage(new SuccessMsg(session));

        for (Message histMsg : room.getHistory()) {
            protocol.writeMessage(histMsg);
        }

        room.broadcast(new EventLoginMsg(username), this);
        broadcastUserList();
        logger.log(username + " entrance the chat");
    }

    private void handleLogout() throws IOException {
        protocol.writeMessage(new SuccessMsg(null));
    }

    private void handleMessage(SendMessageMsg msg) throws IOException {
        if (!isLoggedIn(msg.getSession())) return;

        String text = msg.getText();
        if (text == null || text.isBlank()) {
            sendError("Empty massage");
            return;
        }

        EventMessageMsg event = new EventMessageMsg(username, msg.getText());
        room.broadcast(event, null);
        room.saveToHistory(event);

        protocol.writeMessage(new SuccessMsg(null));
        logger.log(username + ": " + msg.getText());
    }

    private void handleList(ListRequestMsg msg) throws IOException {
        if (!isLoggedIn(msg.getSession())) return;
        protocol.writeMessage(new ListResponseMsg(room.getOnlineUsers()));
    }


    public synchronized void send(Message msg) {
        try {
            protocol.writeMessage(msg);
        } catch (IOException e) {
            logger.log("Ошибка отправки " + username + ": " + e.getMessage());
        }
    }

    public String getUsername() { return username; }
    public String getSession()  { return session; }

    private boolean isLoggedIn(String sessionId) throws IOException {
        if (username == null || !session.equals(sessionId)) {
            sendError("Not authorized");
            return false;
        }
        return true;
    }

    private void sendError(String reason) throws IOException {
        protocol.writeMessage(new ErrorMsg(reason));
    }

    private void broadcastUserList() {
        room.broadcast(new ListResponseMsg(room.getOnlineUsers()), null);
    }

    private void cleanup() {
        if (username != null) {
            room.leave(this);
            room.broadcast(new EventLogoutMsg(username), null);
            broadcastUserList();
            logger.log(username + " exit from the chat");
        }
        try { socket.close(); } catch (IOException ignored) {}
    }


}
