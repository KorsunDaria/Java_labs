package chat.protocol;

import java.io.IOException;

public interface Protocol {

    Message readMessage() throws IOException;
    void writeMessage(Message message) throws IOException;
    void close() throws IOException;
}
