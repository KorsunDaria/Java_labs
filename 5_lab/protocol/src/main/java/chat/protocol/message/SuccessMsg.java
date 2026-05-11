package chat.protocol.message;


public class SuccessMsg extends Message {
    private String session;

    public SuccessMsg() {}
    public SuccessMsg(String session) { this.session = session; }

    @Override public String getType() { return "success"; }

    public String getSession() { return session; }
}
