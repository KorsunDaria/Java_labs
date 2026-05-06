package org.example.server;

import org.example.persistence.HistoryStore;
import org.example.protocol.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


public class ChatRoom {

    private static final Logger log = Logger.getLogger(ChatRoom.class.getName());

    private static final int BACKLOG = 50;

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<Message> history = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong seq = new AtomicLong(0);
    private final HistoryStore store;


    public ChatRoom(HistoryStore store) {
        this.store = store;

        List<Message> saved = store.load();
        history.addAll(saved);
        if (!saved.isEmpty()) {

            seq.set(saved.get(saved.size() - 1).getSeqNum());
        }
        log.info("Loaded " + saved.size() + " messages from history");
    }


    public void join(ClientHandler handler) {
        clients.add(handler);
        log.info(handler.getUsername() + " joined the chat");
        broadcast(Message.of(Message.Type.EVENT_LOGIN, handler.getUsername(), ""), handler);
    }


    public void leave(ClientHandler handler) {
        if (clients.remove(handler)) {
            log.info(handler.getUsername() + " left the chat");
            broadcast(Message.of(Message.Type.EVENT_LOGOUT, handler.getUsername(), ""), null);
        }
    }


    public long publish(Message msg) {
        long num = seq.incrementAndGet();
        Message stamped = new Message(
                msg.getType(), msg.getSender(),
                msg.getBody(), msg.getSession(), num
        );
        history.add(stamped);
        store.append(stamped);
        broadcast(stamped, null);
        return num;
    }


    public List<Message> historyAfter(long afterSeq) {
        List<Message> tail = new ArrayList<>();
        synchronized (history) {
            for (Message m : history) {
                if (m.getSeqNum() > afterSeq) tail.add(m);
            }
        }
        int from = Math.max(0, tail.size() - BACKLOG);
        return tail.subList(from, tail.size());
    }


    public List<Message> recentHistory() {
        return historyAfter(Math.max(0, seq.get() - BACKLOG));
    }


    public String getOnlineList() {
        List<String> names = new ArrayList<>();
        for (ClientHandler h : clients) names.add(h.getUsername());
        return String.join(",", names);
    }


    private void broadcast(Message msg, ClientHandler exclude) {
        for (ClientHandler c : clients) {
            if (c == exclude) continue;
            try {
                c.sendMessage(msg);
            } catch (IOException e) {
                log.fine("Broadcast failed for " + c.getUsername() + ": " + e.getMessage());
            }
        }
    }
}