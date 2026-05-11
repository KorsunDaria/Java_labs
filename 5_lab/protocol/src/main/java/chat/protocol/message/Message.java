package chat.protocol.message;

import java.io.Serializable;


public abstract class Message implements Serializable {

    public abstract String getType();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
