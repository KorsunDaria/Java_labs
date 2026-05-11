package chat.protocol;

import chat.protocol.message.Message;

import java.io.IOException;

public interface Protocol {

    byte MARKER_XML    = 'X';
    byte MARKER_SERIAL = 'S';

    Message readMessage() throws IOException;
    void writeMessage(Message message) throws IOException;
    void close() throws IOException;
}
