package chat.protocol.message;


public class EventMessageMsg extends Message {
    private String fromName;
    private String text;

    public EventMessageMsg() {}
    public EventMessageMsg(String fromName, String text) {
        this.fromName = fromName;
        this.text = text;
    }

    @Override public String getType() { return "event_message"; }

    public String getFromName() { return fromName; }
    public String getText()     { return text; }
}
