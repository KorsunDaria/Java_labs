package chat.protocol;

import java.io.IOException;
import java.net.Socket;

public class ProtocolFactory {

    private ProtocolFactory() {}

    public static Protocol create(Socket socket, boolean useXml) throws IOException {
        socket.getOutputStream().write(useXml ? Protocol.MARKER_XML : Protocol.MARKER_SERIAL);
        socket.getOutputStream().flush();
        return useXml ? new XmlProtocol(socket) : new SerialProtocol(socket);
    }

    public static Protocol detect(Socket socket) throws IOException {
        int marker = socket.getInputStream().read();
        return marker == Protocol.MARKER_SERIAL
                ? new SerialProtocol(socket)
                : new XmlProtocol(socket);
    }
}
