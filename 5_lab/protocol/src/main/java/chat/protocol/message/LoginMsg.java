package chat.protocol.message;


public class LoginMsg extends Message {
    private String name;
    private String password;
    private String clientType;

    public LoginMsg() {}
    public LoginMsg(String name, String password, String clientType) {
        this.name = name;
        this.password = password;
        this.clientType = clientType;
    }

    @Override public String getType() { return "login"; }

    public String getName()       { return name; }
    public String getPassword()   { return password; }
    public String getClientType() { return clientType; }
}
