package chat.protocol.message;


public class ListRequestMsg extends Message {
    private String session;

    public ListRequestMsg() {}
    public ListRequestMsg(String session) { this.session = session; }

    @Override public String getType() { return "list"; }

    public String getSession() { return session; }
}
