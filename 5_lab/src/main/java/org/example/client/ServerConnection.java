package org.example.client;

import org.example.protocol.Message;
import org.example.protocol.Transport;
import org.example.protocol.XmlTransport;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Logger;



public class ServerConnection {

    private static final Logger log = Logger.getLogger(ServerConnection.class.getName());
    private static final int RETRY_DELAY_MS = 3_000;

    private final String          host;
    private final int             port;
    private final String          username;
    private final String          password;
    private final Consumer<Message> onMessage;

    private Transport transport;
    private String    sessionId  = "";
    private long      lastSeqNum = 0;
    private volatile boolean running = true;


    public ServerConnection(String host, int port, String username,
                            String password, Consumer<Message> onMessage) {
        this.host      = host;
        this.port      = port;
        this.username  = username;
        this.password  = password;
        this.onMessage = onMessage;
    }


    public void start() {
        Thread.ofVirtual().name("server-connection").start(this::connectionLoop);
    }


    public void stop() {
        running = false;
        closeTransport();
    }


    public void send(Message msg) throws IOException {
        if (transport == null) throw new IOException("Not connected");
        transport.send(msg);
    }


    public String getSessionId() { return sessionId; }



    private void connectionLoop() {
        while (running) {
            try {
                connect();

                onMessage.accept(Message.of(Message.Type.SUCCESS, username, sessionId));
                readLoop();
            } catch (AuthException e) {

                log.warning("Auth error: " + e.getMessage());
                closeTransport();
                onMessage.accept(Message.of(Message.Type.ERROR, "", e.getMessage()));
                running = false;
                break;
            } catch (IOException e) {
                log.warning("Connection lost: " + e.getMessage());
                closeTransport();
                if (!running) break;
                notifyReconnecting();
                sleep(RETRY_DELAY_MS);
            }
        }
    }

    private void connect() throws IOException {
        Socket socket = new Socket(host, port);
        transport = new XmlTransport(socket);

        log.info("0 ");

        Message login = new Message(
                Message.Type.LOGIN, username, "JavaChat", password, lastSeqNum
        );
        log.info("1 ");
        transport.send(login);
        log.info("2 ");
        Message response = transport.receive();
        log.info("2,5 ");
        if (response == null || response.getType() == Message.Type.ERROR) {
            String reason = response != null ? response.getBody() : "no response";
            throw new AuthException(reason);
        }
        log.info("3 ");

        sessionId = response.getBody();
        log.info("Connected as " + username + ", session=" + sessionId);
    }

    private void readLoop() throws IOException {
        while (running) {
            Message msg = transport.receive();
            if (msg == null) break;


            if (msg.getSeqNum() > 0) lastSeqNum = msg.getSeqNum();

            onMessage.accept(msg);
        }
    }

    private void notifyReconnecting() {
        onMessage.accept(Message.of(Message.Type.EVENT_MESSAGE,
                "System", "Reconnecting in " + (RETRY_DELAY_MS / 1000) + "s..."));
    }

    private void closeTransport() {
        if (transport != null) {
            try { transport.close(); } catch (Exception ignored) {}
            transport = null;
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}