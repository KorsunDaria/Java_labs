package chat.server;

import chat.protocol.Protocol;
import chat.protocol.SerialProtocol;
import chat.protocol.XmlProtocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class Server {

    public static void main(String[] args) throws IOException {
        Properties config = loadConfig();

        int     port        = Integer.parseInt(config.getProperty("port", "12345"));
        boolean logging     = Boolean.parseBoolean(config.getProperty("logging", "true"));
        int     historySize = Integer.parseInt(config.getProperty("history_size", "50"));
        String  historyFile = config.getProperty("history_file", "chat_history.txt");
        String  usersFile   = config.getProperty("users_file", "users.properties");

        ServerLogger    logger  = new ServerLogger(logging);
        MessageHistory  history = new MessageHistory(historyFile, historySize);
        UserStore       users   = new UserStore(usersFile);
        ChatRoom        room    = new ChatRoom(logger, history);

        logger.log("Start server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                logger.log("New socket: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, room, users, logger);
                Thread.ofVirtual().start(handler);
            }
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();

        props.setProperty("port",         "12345");
        props.setProperty("logging",      "true");
        props.setProperty("history_size", "50");
        props.setProperty("history_file", "chat_history.txt");
        props.setProperty("users_file",   "users.properties");

        try (FileInputStream fis = new FileInputStream("server.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println(" can not find server.properties");
        }
        return props;
    }

    public static Protocol detectProtocol(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        int marker = in.read();
        if (marker == Protocol.MARKER_SERIAL) {
            return new SerialProtocol(socket);
        }
        return new XmlProtocol(socket);
    }
}
