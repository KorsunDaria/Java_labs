package org.example.protocol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;


public class XmlTransport implements Transport {

    private final DataOutputStream out;
    private final DataInputStream  in;
    private final Socket           socket;

    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory     TF  = TransformerFactory.newInstance();

    public XmlTransport(Socket socket) throws IOException {
        this.socket = socket;
        this.out    = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in     = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }



    @Override
    public synchronized void send(Message msg) throws IOException {
        try {
            byte[] xml = toXml(msg).getBytes(StandardCharsets.UTF_8);
            out.writeInt(xml.length);
            out.write(xml);
            out.flush();
        } catch (Exception e) {
            throw new IOException("Failed to send message", e);
        }
    }



    @Override
    public Message receive() throws IOException {
        try {
            int len = in.readInt();
            if (len <= 0 || len > 1 << 20) {
                return null;
            }
            byte[] buf = new byte[len];
            in.readFully(buf);
            return fromXml(new String(buf, StandardCharsets.UTF_8));
        } catch (EOFException e) {
            return null;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to receive message", e);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }


    private Message fromXml(String xml) throws Exception {
        Document doc = DBF.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Element root = doc.getDocumentElement();
        String tag = root.getTagName();

        switch (tag) {
            case "command" -> {
                String name = root.getAttribute("name");
                return switch (name) {
                    case "login"   -> parseLogin(root);
                    case "logout"  -> parseLogout(root);
                    case "message" -> parseChatMsg(root);
                    case "list"    -> parseList(root);

                    default        -> Message.of(Message.Type.ERROR, "", "unknown command: " + name);
                };
            }
            case "event" -> {
                String evtName = root.getAttribute("name");
                return switch (evtName) {
                    case "message"   -> new Message(
                            Message.Type.EVENT_MESSAGE,
                            text(root, "name"), text(root, "message"), null, 0);
                    case "userlogin" -> Message.of(
                            Message.Type.EVENT_LOGIN,  text(root, "name"), "");
                    case "userlogout"-> Message.of(
                            Message.Type.EVENT_LOGOUT, text(root, "name"), "");
                    default -> Message.of(Message.Type.ERROR, "", "unknown event: " + evtName);
                };
            }
            case "success" -> {
                String session = text(root, "session");
                return new Message(Message.Type.SUCCESS, "", session, session, 0);
            }
            case "error" -> {
                String reason = text(root, "message");
                return Message.of(Message.Type.ERROR, "", reason);
            }
            default -> {
                return Message.of(Message.Type.ERROR, "", "unknown tag: " + tag);
            }
        }
    }

    private Message parseLogin(Element root) {
        String name = text(root, "name");
        String type = text(root, "type");

        String pass = text(root, "password");

        return new Message(Message.Type.LOGIN, name, type, pass, 0);
    }

    private Message parseLogout(Element root) {
        String session = text(root, "session");
        return new Message(Message.Type.LOGOUT, "", "", session, 0);
    }

    private Message parseChatMsg(Element root) {
        String text    = text(root, "message");
        String session = text(root, "session");
        return new Message(Message.Type.CHAT, "", text, session, 0);
    }

    private Message parseList(Element root) {
        String session = text(root, "session");
        return new Message(Message.Type.LIST_REQUEST, "", "", session, 0);
    }


    private String toXml(Message msg) throws Exception {
        Document doc = DBF.newDocumentBuilder().newDocument();
        Element root;

        switch (msg.getType()) {
            case SUCCESS -> {
                root = doc.createElement("success");
                if (msg.getBody() != null && !msg.getBody().isEmpty()) {
                    Element session = doc.createElement("session");
                    session.setTextContent(msg.getBody());
                    root.appendChild(session);
                }
            }
            case ERROR -> {
                root = doc.createElement("error");
                Element reason = doc.createElement("message");
                reason.setTextContent(msg.getBody());
                root.appendChild(reason);
            }
            case EVENT_MESSAGE -> {
                root = doc.createElement("event");
                root.setAttribute("name", "message");
                appendChild(doc, root, "message", msg.getBody());
                appendChild(doc, root, "name", msg.getSender());
            }
            case EVENT_LOGIN -> {
                root = doc.createElement("event");
                root.setAttribute("name", "userlogin");
                appendChild(doc, root, "name", msg.getSender());
            }
            case EVENT_LOGOUT -> {
                root = doc.createElement("event");
                root.setAttribute("name", "userlogout");
                appendChild(doc, root, "name", msg.getSender());
            }
            case LIST_RESPONSE -> {
                // список пользователей — body содержит JSON-строку имён через запятую
                root = doc.createElement("success");
                Element listEl = doc.createElement("listusers");
                for (String u : msg.getBody().split(",")) {
                    if (!u.isBlank()) {
                        Element userEl = doc.createElement("user");
                        appendChild(doc, userEl, "name", u.trim());
                        appendChild(doc, userEl, "type", "JavaChat");
                        listEl.appendChild(userEl);
                    }
                }
                root.appendChild(listEl);
            }case LOGIN -> {
                root = doc.createElement("command");
                root.setAttribute("name", "login");
                appendChild(doc, root, "name",     msg.getSender());
                appendChild(doc, root, "type",     msg.getBody());
                appendChild(doc, root, "password", msg.getSession()); // пароль
            } case CHAT -> {
                root = doc.createElement("command");
                root.setAttribute("name", "message");
                appendChild(doc, root, "message", msg.getBody());
                appendChild(doc, root, "session", msg.getSession());
            } case LIST_REQUEST -> {
                root = doc.createElement("command");
                root.setAttribute("name", "list");
                appendChild(doc, root, "session", msg.getSession());
            }
            case LOGOUT -> {
                root = doc.createElement("command");
                root.setAttribute("name", "logout");
                appendChild(doc, root, "session", msg.getSession());
            }
            default -> {
                root = doc.createElement("error");
                appendChild(doc, root, "message", "unexpected type: " + msg.getType());
            }
        }

        doc.appendChild(root);

        Transformer tf = TF.newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter sw = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }


    private static String text(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent().trim() : "";
    }

    private static void appendChild(Document doc, Element parent, String tag, String value) {
        Element el = doc.createElement(tag);
        el.setTextContent(value);
        parent.appendChild(el);
    }
}