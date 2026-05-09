package chat.protocol;

import java.io.Serializable;



public class Message implements Serializable {

    public static final String LOGIN         = "login";
    public static final String LOGOUT        = "logout";
    public static final String MESSAGE       = "message";
    public static final String LIST          = "list";
    public static final String SUCCESS       = "success";
    public static final String ERROR         = "error";
    public static final String EVENT_MESSAGE = "event_message";
    public static final String EVENT_LOGIN   = "event_login";
    public static final String EVENT_LOGOUT  = "event_logout";
    public static final String LIST_RESPONSE = "list_response";

    private String type;
    private String name;
    private String text;
    private String session;
    private String clientType;
    private String[] users;

    public Message() {}

    public Message(String type) {
        this.type = type;
    }


    public String getType()                  { return type; }
    public void   setType(String type)       { this.type = type; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public String getText()                  { return text; }
    public void   setText(String text)       { this.text = text; }

    public String getSession()               { return session; }
    public void   setSession(String session) { this.session = session; }

    public String getClientType()                      { return clientType; }
    public void   setClientType(String clientType)     { this.clientType = clientType; }

    public String[] getUsers()                         { return users; }
    public void     setUsers(String[] users)           { this.users = users; }

    @Override
    public String toString() {
        return "Message{type=" + type + ", name=" + name + ", text=" + text + "}";
    }
}
