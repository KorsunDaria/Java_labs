package chat.client;

import chat.protocol.*;
import chat.protocol.message.*;

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


    public void tryLogin() throws IOException, AuthException {
        connect();
        login();
    }

    public void startReading(ChatWindow window) {
        this.window = window;
        Thread.ofVirtual().start(this::readLoop);
    }

    public void sendMessage(String text) {
        if (session == null) return;
        send(new SendMessageMsg(text, session));
    }


    public void requestUserList() {
        if (session == null) return;
        send(new ListRequestMsg(session));
    }

    public void disconnect() {
        running = false;
        if (session != null) send(new LogoutMsg(session));
        closeProtocol();
    }


    private void readLoop() {
        window.showStatus("Подключён как " + username);
        requestUserList();

        while (running) {
            try {
                handleIncoming(protocol.readMessage());
            } catch (IOException e) {
                if (!running) break;

                session = null;
                closeProtocol();
                window.showStatus("Соединение потеряно. Переподключение через 3 сек...");
                sleep(RECONNECT_DELAY_MS);


                while (running) {
                    try {
                        connect();
                        login();
                        window.showStatus("Подключён как " + username);
                        requestUserList();
                        break;
                    } catch (AuthException ae) {
                        running = false;
                        window.showStatus("Ошибка авторизации при переподключении: " + ae.getMessage());
                        return;
                    } catch (IOException ioe) {
                        window.showStatus("Нет связи. Повтор через 3 сек...");
                        sleep(RECONNECT_DELAY_MS);
                    }
                }
            }
        }
    }

    private void connect() throws IOException {
        Socket socket = new Socket(host, port);
        protocol = ProtocolFactory.create(socket, useXml);
    }

    private void login() throws IOException, AuthException {
        protocol.writeMessage(new LoginMsg(username, password, "SwingClient"));

        while (true) {
            Message response = protocol.readMessage();
            if (response instanceof SuccessMsg m) {
                this.session = m.getSession();
                return;
            }
            if (response instanceof ErrorMsg m) {
                throw new AuthException(m.getReason());
            }
        }
    }

    private void handleIncoming(Message msg) {
        if (msg instanceof EventMessageMsg m) {
            window.appendMessage(m.getFromName(), m.getText());
        } else if (msg instanceof EventLoginMsg m) {
            window.appendEvent(m.getName() + " вошёл в чат");
        } else if (msg instanceof EventLogoutMsg m) {
            window.appendEvent(m.getName() + " покинул чат");
        } else if (msg instanceof ListResponseMsg m) {
            window.updateUserList(m.getUsers());
        }
    }

    private void send(Message msg) { //s
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

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }
}
