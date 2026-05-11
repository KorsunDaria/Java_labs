package chat.protocol.message;

public class EventLogoutMsg extends Message {
    private String name;

    public EventLogoutMsg() {}
    public EventLogoutMsg(String name) { this.name = name; }

    @Override public String getType() { return "event_logout"; }

    public String getName() { return name; }
}
