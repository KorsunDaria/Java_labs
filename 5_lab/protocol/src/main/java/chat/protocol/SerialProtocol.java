package chat.protocol;

import chat.protocol.message.Message;

import java.io.*;
import java.net.Socket;


public class SerialProtocol implements Protocol {

    private final ObjectInputStream  in;
    private final ObjectOutputStream out;

    public SerialProtocol(Socket socket) throws IOException {
        this.out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.out.flush();
        this.in  = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public Message readMessage() throws IOException {
        try {
            return (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error: Can not understand class", e);
        }
    }

    @Override
    public void writeMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}
