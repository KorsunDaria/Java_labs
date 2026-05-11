package chat.protocol.message;


public class ErrorMsg extends Message {
    private String reason;

    public ErrorMsg() {}
    public ErrorMsg(String reason) { this.reason = reason; }

    @Override public String getType() { return "error"; }

    public String getReason() { return reason; }
}
