package chat.loadtest;

import chat.protocol.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Минимальный клиент для нагрузочных тестов.
 * Подключается, логинится и умеет отправлять сообщения.
 */
public class TestClient implements AutoCloseable {

    private final Socket   socket;
    private final Protocol protocol;
    private       String   session;
    private final String   username;

    public TestClient(String host, int port, String username) throws IOException {
        this.username = username;
        this.socket   = new Socket(host, port);

        // шлём маркер XML протокола
        OutputStream raw = socket.getOutputStream();
        raw.write(Protocol.MARKER_XML);
        raw.flush();

        this.protocol = new XmlProtocol(socket);
        login();
    }

    private void login() throws IOException {
        protocol.writeMessage(new LoginMsg(username, "testpass", "TestClient"));
        Message response = protocol.readMessage();
        if (response instanceof ErrorMsg m) {
            throw new IOException("Ошибка входа: " + m.getReason());
        }
        this.session = ((SuccessMsg) response).getSession();
    }

    public void sendMessage(String text) throws IOException {
        protocol.writeMessage(new SendMessageMsg(text, session));
        protocol.readMessage(); // читаем SuccessMsg
    }

    /**
     * Прочитать одно входящее сообщение (не блокирует надолго — таймаут 1 сек).
     */
    public Message readMessage() throws IOException {
        return protocol.readMessage();
    }

    public String getUsername() { return username; }
    public Socket  getSocket()   { return socket; }

    @Override
    public void close() throws IOException {
        try {
            if (session != null) protocol.writeMessage(new LogoutMsg(session));
        } catch (IOException ignored) {}
        socket.close();
    }
}
