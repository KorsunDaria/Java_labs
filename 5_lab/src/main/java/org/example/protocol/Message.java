package org.example.protocol;

import java.io.Serializable;

public class Message implements Serializable {

    public enum Type {
        LOGIN, LOGOUT, CHAT, LIST_REQUEST, LIST_RESPONSE,
        EVENT_MESSAGE, EVENT_LOGIN, EVENT_LOGOUT,
        SUCCESS, ERROR
    }

    private final Type   type;
    private final String sender;
    private final String body;
    private final String session;
    private final long   seqNum;

    public Message(Type type, String sender, String body, String session, long seqNum) {
        this.type    = type;
        this.sender  = sender;
        this.body    = body;
        this.session = session;
        this.seqNum  = seqNum;
    }

    /** Быстрый конструктор для случаев без session и seqNum. */
    public static Message of(Type type, String sender, String body) {
        return new Message(type, sender, body, null, 0);
    }

    public Type   getType()    { return type; }
    public String getSender()  { return sender; }
    public String getBody()    { return body; }
    public String getSession() { return session; }
    public long   getSeqNum()  { return seqNum; }

    @Override
    public String toString() {
        return "[" + type + "] " + sender + ": " + body;
    }
}