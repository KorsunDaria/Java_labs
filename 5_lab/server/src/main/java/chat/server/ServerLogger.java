package chat.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ServerLogger {

    private final boolean enabled;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerLogger(boolean enabled) {
        this.enabled = enabled;
    }

    public void log(String message) {
        if (!enabled) return;
        String time = LocalDateTime.now().format(FMT);
        System.out.println("[" + time + "] " + message);
    }
}
