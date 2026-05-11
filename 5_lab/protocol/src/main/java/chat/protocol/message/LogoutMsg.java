package chat.protocol.message;


public class LogoutMsg extends Message {
    private String session;

    public LogoutMsg() {}
    public LogoutMsg(String session) { this.session = session; }

    @Override public String getType() { return "logout"; }

    public String getSession() { return session; }
}
