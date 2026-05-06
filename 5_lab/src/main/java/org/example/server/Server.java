package org.example.server;

import org.example.auth.UserStore;
import org.example.persistence.HistoryStore;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.logging.*;


public class Server {

    private static final Logger log = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) throws IOException {
        Properties cfg = loadConfig("server.properties");

        int     port    = Integer.parseInt(cfg.getProperty("port", "12345"));
        boolean logging = Boolean.parseBoolean(cfg.getProperty("logging", "true"));
        String  historyPath = cfg.getProperty("history.file", "data/history.json");
        String  usersPath   = cfg.getProperty("users.file",   "data/users.json");

        if (!logging) {
            Logger.getLogger("").setLevel(Level.OFF);
        } else {
            Logger.getLogger("").setLevel(Level.ALL);
        }

        UserStore    users   = new UserStore(usersPath);
        HistoryStore history = new HistoryStore(historyPath);
        ChatRoom     room    = new ChatRoom(history);

        try (ServerSocket server = new ServerSocket(port)) {
            log.info("Chat server started on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket client = server.accept();
                client.setSoTimeout(60_000); // таймаут неактивности 60 сек
                log.info("New connection from " + client.getRemoteSocketAddress());

                Thread.ofVirtual()
                        .name("client-" + client.getRemoteSocketAddress())
                        .start(new ClientHandler(client, room, users));
            }
        }
    }

    private static Properties loadConfig(String path) {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            p.load(in);
            log.info("Config loaded from " + path);
        } catch (IOException e) {
            log.warning("server.properties not found, using defaults");
        }
        return p;
    }
}