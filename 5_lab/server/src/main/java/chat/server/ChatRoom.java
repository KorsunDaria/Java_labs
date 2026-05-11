package chat.server;

import chat.protocol.message.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoom {

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final ServerLogger        logger;
    private final MessageHistory      history;

    public ChatRoom(ServerLogger logger, MessageHistory history) {
        this.logger  = logger;
        this.history = history;
    }


    public void join(ClientHandler handler) {
        clients.add(handler);
        logger.log("Numder of members: " + clients.size());
    }

    public void leave(ClientHandler handler) {
        clients.remove(handler);
        logger.log("Numder of members: " + clients.size());
    }

    public boolean isOnline(String username) {
        return clients.stream().anyMatch(c -> username.equals(c.getUsername()));
    }


    public void broadcast(Message message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.send(message);
            }
        }
    }


    public String[] getOnlineUsers() {
        return clients.stream()
                .map(ClientHandler::getUsername)
                .filter(name -> name != null)
                .toArray(String[]::new);
    }


    public List<EventMessageMsg> getHistory() {
        return history.getLast();
    }

    public void saveToHistory(EventMessageMsg msg) {
        history.append(msg);
    }
}
