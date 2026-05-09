package chat.client;

import chat.protocol.*;

import java.io.IOException;
import java.net.Socket;


public class Connection {

    private static final int RECONNECT_DELAY_MS = 3000;

    private final String  host;
    private final int     port;
    private final boolean useXml;
    private final String  username;
    private final String  password;

    private Protocol   protocol;
    private String     session;
    private ChatWindow window;
    private volatile boolean running = true;

    public Connection(String host, int port, boolean useXml, String username, String password) {
        this.host     = host;
        this.port     = port;
        this.useXml   = useXml;
        this.username = username;
        this.password = password;
    }

    public void setWindow(ChatWindow window) {
        this.window = window;
    }

    public void start() {
        Thread.ofVirtual().start(this::connectLoop);
    }

    public void sendMessage(String text) {
        if (session == null) return;
        Message msg = new Message(Message.MESSAGE);
        msg.setText(text);
        msg.setSession(session);
        send(msg);
    }

    public void requestUserList() {
        if (session == null) return;
        Message msg = new Message(Message.LIST);
        msg.setSession(session);
        send(msg);
    }

    public void disconnect() {
        running = false;
        if (session != null && protocol != null) {
            Message msg = new Message(Message.LOGOUT);
            msg.setSession(session);
            send(msg);
        }
        closeProtocol();
    }


    private void connectLoop() {
        while (running) {
            try {
                window.showStatus("Connecting to " + host + ":" + port + "...");
                connect();
                login();
                window.showStatus("Connecting as " + username);
                requestUserList();
                readLoop();
            } catch (AuthException e) {
                running = false;
                closeProtocol();
                window.showAuthError(e.getMessage());
                return;
            } catch (IOException e) {
                if (!running) break;
                session = null;
                closeProtocol();
                window.showStatus("Connection lost...");
                sleep(RECONNECT_DELAY_MS);
            }
        }
    }

    private void connect() throws IOException {
        Socket socket = new Socket(host, port);
        protocol = useXml ? new XmlProtocol(socket) : new SerialProtocol(socket);
    }

    private void login() throws IOException, AuthException {
        Message msg = new Message(Message.LOGIN);
        msg.setName(username);
        msg.setText(password);
        msg.setClientType("SwingClient");
        protocol.writeMessage(msg);

        Message response = protocol.readMessage();
        if (Message.ERROR.equals(response.getType())) {
            throw new AuthException(response.getText());
        }
        this.session = response.getSession();
    }

    private void readLoop() throws IOException {
        while (running) {
            Message msg = protocol.readMessage();
            handleIncoming(msg);
        }
    }

    private void handleIncoming(Message msg) {
        switch (msg.getType()) {
            case Message.EVENT_MESSAGE  -> window.appendMessage(msg.getName(), msg.getText());
            case Message.EVENT_LOGIN -> {
                window.appendEvent(msg.getName() + " Entered chat");
            }
            case Message.EVENT_LOGOUT -> {
                window.appendEvent(msg.getName() + " Left chat");

            }
            case Message.LIST_RESPONSE  -> window.updateUserList(msg.getUsers());
        }
    }

    private synchronized void send(Message msg) {
        if (protocol == null) return;
        try {
            protocol.writeMessage(msg);
        } catch (IOException ignored) {}
    }

    private void closeProtocol() {
        if (protocol != null) {
            try { protocol.close(); } catch (IOException ignored) {}
            protocol = null;
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public boolean isXml() { return useXml; }


    static class AuthException extends Exception {
        AuthException(String message) { super(message); }
    }
}
