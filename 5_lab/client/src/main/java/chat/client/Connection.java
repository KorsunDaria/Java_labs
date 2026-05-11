package chat.client;

import chat.protocol.*;
import chat.protocol.message.*;

import java.io.IOException;
import java.io.OutputStream;
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
        OutputStream raw = socket.getOutputStream();
        raw.write(useXml ? Protocol.MARKER_XML : Protocol.MARKER_SERIAL);
        raw.flush();

        protocol = useXml ? new XmlProtocol(socket) : new SerialProtocol(socket);
    }

    private void login() throws IOException, AuthException {
        protocol.writeMessage(new LoginMsg(username, password, "SwingClient"));

        Message response = protocol.readMessage();
        if (response instanceof ErrorMsg m) {
            throw new AuthException(m.getReason());
        }
        this.session = ((SuccessMsg) response).getSession();
    }

    private void readLoop() throws IOException {
        while (running) {
            handleIncoming(protocol.readMessage());
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
