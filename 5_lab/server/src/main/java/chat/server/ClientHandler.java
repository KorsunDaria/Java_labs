package chat.server;

import chat.protocol.*;

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

            socket.setSoTimeout(5000);
            int firstByte = socket.getInputStream().read();
            socket.setSoTimeout(TIMEOUT_MS);

            if (firstByte == 0xAC) {
                protocol = new SerialProtocol(new PeekedSocket(socket, (byte) firstByte));
            } else {
                protocol = new XmlProtocol(new PeekedSocket(socket, (byte) firstByte));
            }

            handleMessages();

        } catch (IOException e) {
            logger.log("Сдшуте disconnected (timeout): " +
                    (username != null ? username : socket.getRemoteSocketAddress()));
        } finally {
            cleanup();
        }
    }

    private void handleMessages() throws IOException {
        while (true) {
            Message msg = protocol.readMessage();
            logger.log("Take [" + (username != null ? username : "?") + "]: " + msg);

            switch (msg.getType()) {
                case Message.LOGIN   -> handleLogin(msg);
                case Message.LOGOUT  -> { handleLogout(); return; }
                case Message.MESSAGE -> handleMessage(msg);
                case Message.LIST    -> handleList(msg);
                default -> sendError("Incorrect massage type: " + msg.getType());
            }
        }
    }

    private void handleLogin(Message msg) throws IOException {
        String name = msg.getName();
        String password = msg.getText();

        if (name == null || name.isBlank()) {
            sendError("Name could be not empty");
            return;
        }

        String error = users.authenticate(name, password);
        if (error != null) {
            sendError(error);
            return;
        }

        if (room.isOnline(name)) {
            sendError("Dubliccate name");
            return;
        }

        this.username = name;
        this.session  = java.util.UUID.randomUUID().toString();

        room.join(this);


        Message ok = new Message(Message.SUCCESS);
        ok.setSession(session);
        protocol.writeMessage(ok);


        for (Message histMsg : room.getHistory()) {
            protocol.writeMessage(histMsg);
        }

        room.broadcast(eventLogin(name), this);
        logger.log(name + " entrance the chat");
        broadcastUserList();
    }

    private void handleLogout() throws IOException {
        protocol.writeMessage(new Message(Message.SUCCESS));
    }

    private void handleMessage(Message msg) throws IOException {
        if (!isLoggedIn(msg.getSession())) return;

        String text = msg.getText();
        if (text == null || text.isBlank()) {
            sendError("Empty massage");
            return;
        }

        Message event = new Message(Message.EVENT_MESSAGE);
        event.setName(username);
        event.setText(text);

        room.broadcast(event, null);
        room.saveToHistory(event);

        protocol.writeMessage(new Message(Message.SUCCESS));
        logger.log(username + ": " + text);
    }

    private void handleList(Message msg) throws IOException {
        if (!isLoggedIn(msg.getSession())) return;

        String[] onlineUsers = room.getOnlineUsers();
        Message response = new Message(Message.LIST_RESPONSE);
        response.setUsers(onlineUsers);
        protocol.writeMessage(response);
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
        Message err = new Message(Message.ERROR);
        err.setText(reason);
        protocol.writeMessage(err);
    }

    private void cleanup() {
        if (username != null) {
            room.leave(this);
            room.broadcast(eventLogout(username), null);
            logger.log(username + " left the chat");
            broadcastUserList();
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    private Message eventLogin(String name) {
        Message m = new Message(Message.EVENT_LOGIN);
        m.setName(name);
        return m;
    }

    private Message eventLogout(String name) {
        Message m = new Message(Message.EVENT_LOGOUT);
        m.setName(name);
        return m;
    }

    private void broadcastUserList() {
        Message list = new Message(Message.LIST_RESPONSE);
        list.setUsers(room.getOnlineUsers());
        room.broadcast(list, null);
    }
}
