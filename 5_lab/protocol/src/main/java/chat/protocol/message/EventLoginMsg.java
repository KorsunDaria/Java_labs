package chat.protocol.message;


public class EventLoginMsg extends Message {
    private String name;

    public EventLoginMsg() {}
    public EventLoginMsg(String name) { this.name = name; }

    @Override public String getType() { return "event_login"; }

    public String getName() { return name; }
}
