package chat.protocol.message;

public class ListResponseMsg extends Message {
    private String[] users;

    public ListResponseMsg() {}
    public ListResponseMsg(String[] users) { this.users = users; }

    @Override public String getType() { return "list_response"; }

    public String[] getUsers() { return users; }
}
