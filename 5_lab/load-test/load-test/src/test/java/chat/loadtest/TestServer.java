package chat.loadtest;

import chat.protocol.*;
import chat.server.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Поднимает настоящий сервер на случайном свободном порту для тестов.
 * Использование:
 *   TestServer server = new TestServer();
 *   server.start();
 *   // ... тест ...
 *   server.stop();
 */
public class TestServer {

    private final int        port;
    private final ChatRoom   room;
    private final UserStore  users;
    private ServerSocket     serverSocket;
    private Thread           acceptThread;
    private volatile boolean running;

    public TestServer() throws IOException {
        // порт 0 — ОС сама выберет свободный
        this.serverSocket = new ServerSocket(0);
        this.port         = serverSocket.getLocalPort();

        ServerLogger logger = new ServerLogger(false); // в тестах логи не нужны
        MessageHistory history = new MessageHistory("test_history_" + port + ".txt", 50);
        this.users = new UserStore("test_users_" + port + ".properties");
        this.room  = new ChatRoom(logger, history);
    }

    public void start() {
        running = true;
        acceptThread = Thread.ofVirtual().start(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    Thread.ofVirtual().start(new ClientHandler(socket, room, users, new ServerLogger(false)));
                } catch (IOException e) {
                    if (running) System.err.println("Accept error: " + e.getMessage());
                }
            }
        });
    }

    public void stop() throws IOException {
        running = false;
        serverSocket.close();
        // подчищаем тестовые файлы
        new java.io.File("test_history_" + port + ".txt").delete();
        new java.io.File("test_users_"   + port + ".properties").delete();
    }

    public int getPort() { return port; }

    /**
     * Создать подключённого тестового клиента (XML протокол).
     */
    public TestClient connectClient(String username) throws IOException {
        return new TestClient("localhost", port, username);
    }
}
