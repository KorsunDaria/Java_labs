package chat.protocol;

import chat.protocol.message.*;
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
import java.nio.charset.StandardCharsets;

public class XmlProtocol implements Protocol {

    private final DataInputStream  in;
    private final DataOutputStream out;

    public XmlProtocol(Socket socket) throws IOException {
        this.in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public Message readMessage() throws IOException {
        int length = in.readInt();
        if (length <= 0 || length > 1024 * 1024) {
            throw new IOException("Incorrect length: " + length);
        }
        byte[] bytes = in.readNBytes(length);
        String xml = new String(bytes, StandardCharsets.UTF_8);
        return parseXml(xml);
    }

    @Override
    public void writeMessage(Message message) throws IOException {
        String xml = toXml(message);
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }

    private Message parseXml(String xml) throws IOException {
        try {
            Element root = parseRoot(xml);
            return switch (root.getTagName()) {
                case "command" -> parseCommand(root);
                case "event"   -> parseEvent(root);
                case "success" -> parseSuccess(root);
                case "error"   -> new ErrorMsg(getText(root, "message"));
                default -> throw new IOException("Неизвестный тег: " + root.getTagName());
            };
        }catch (IOException e) {
            throw e;
        }  catch (Exception e) {
            throw new IOException("Error with parse XML: " + e.getMessage(), e);
        }
    }
    private Message parseCommand(Element root) throws IOException {
        return switch (root.getAttribute("name")) {
            case "login"   -> new LoginMsg(
                    getText(root, "name"),
                    getText(root, "password"),
                    getText(root, "type"));
            case "logout"  -> new LogoutMsg(getText(root, "session"));
            case "message" -> new SendMessageMsg(getText(root, "message"), getText(root, "session"));
            case "list"    -> new ListRequestMsg(getText(root, "session"));
            default -> throw new IOException("Неизвестная команда: " + root.getAttribute("name"));
        };
    }

    private Message parseEvent(Element root) throws IOException {
        return switch (root.getAttribute("name")) {
            case "message"    -> new EventMessageMsg(getText(root, "name"), getText(root, "message"));
            case "userlogin"  -> new EventLoginMsg(getText(root, "name"));
            case "userlogout" -> new EventLogoutMsg(getText(root, "name"));
            default -> throw new IOException("Неизвестное событие: " + root.getAttribute("name"));
        };
    }

    private Message parseSuccess(Element root) {
        NodeList userNodes = root.getElementsByTagName("user");
        if (userNodes.getLength() > 0) {
            String[] users = new String[userNodes.getLength()];
            for (int i = 0; i < userNodes.getLength(); i++) {
                users[i] = getText((Element) userNodes.item(i), "name");
            }
            return new ListResponseMsg(users);
        }
        return new SuccessMsg(getText(root, "session"));
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() == 0 ? null : list.item(0).getTextContent();
    }


    private String toXml(Message msg) throws IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(buildElement(doc, msg));

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StringWriter sw = new StringWriter();
            tf.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new IOException("Ошибка сериализации XML: " + e.getMessage(), e);
        }
    }


    private Element buildElement(Document doc, Message msg) throws IOException {
        if (msg instanceof LoginMsg m) {
            Element e = command(doc, "login");
            addChild(doc, e, "name",     m.getName());
            addChild(doc, e, "password", m.getPassword());
            addChild(doc, e, "type",     m.getClientType() != null ? m.getClientType() : "SwingClient");
            return e;
        }
        if (msg instanceof LogoutMsg m) {
            Element e = command(doc, "logout");
            addChild(doc, e, "session", m.getSession());
            return e;
        }
        if (msg instanceof SendMessageMsg m) {
            Element e = command(doc, "message");
            addChild(doc, e, "message", m.getText());
            addChild(doc, e, "session", m.getSession());
            return e;
        }
        if (msg instanceof ListRequestMsg m) {
            Element e = command(doc, "list");
            addChild(doc, e, "session", m.getSession());
            return e;
        }
        if (msg instanceof SuccessMsg m) {
            Element e = doc.createElement("success");
            if (m.getSession() != null) addChild(doc, e, "session", m.getSession());
            return e;
        }
        if (msg instanceof ListResponseMsg m) {
            Element e = doc.createElement("success");
            Element list = doc.createElement("listusers");
            if (m.getUsers() != null) {
                for (String u : m.getUsers()) {
                    Element user = doc.createElement("user");
                    addChild(doc, user, "name", u);
                    addChild(doc, user, "type", "SwingClient");
                    list.appendChild(user);
                }
            }
            e.appendChild(list);
            return e;
        }
        if (msg instanceof ErrorMsg m) {
            Element e = doc.createElement("error");
            addChild(doc, e, "message", m.getReason());
            return e;
        }
        if (msg instanceof EventMessageMsg m) {
            Element e = event(doc, "message");
            addChild(doc, e, "message", m.getText());
            addChild(doc, e, "name",    m.getFromName());
            return e;
        }
        if (msg instanceof EventLoginMsg m) {
            Element e = event(doc, "userlogin");
            addChild(doc, e, "name", m.getName());
            return e;
        }
        if (msg instanceof EventLogoutMsg m) {
            Element e = event(doc, "userlogout");
            addChild(doc, e, "name", m.getName());
            return e;
        }
        throw new IOException("Неизвестный тип сообщения: " + msg.getClass());
    }

    private Element parseRoot(String xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return doc.getDocumentElement();
    }

    private Element command(Document doc, String name) {
        Element e = doc.createElement("command");
        e.setAttribute("name", name);
        return e;
    }

    private Element event(Document doc, String name) {
        Element e = doc.createElement("event");
        e.setAttribute("name", name);
        return e;
    }

    private void addChild(Document doc, Element parent, String tag, String text) {
        if (text == null) return;
        Element child = doc.createElement(tag);
        child.setTextContent(text);
        parent.appendChild(child);
    }
}
