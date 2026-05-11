package chat.protocol;


import chat.protocol.message.Message;

public class SendMessageMsg extends Message {
    private String text;
    private String session;

    public SendMessageMsg() {}
    public SendMessageMsg(String text, String session) {
        this.text = text;
        this.session = session;
    }

    @Override public String getType() { return "message"; }

    public String getText()    { return text; }
    public String getSession() { return session; }
}
