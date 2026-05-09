package chat.protocol;

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
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            Element root = doc.getDocumentElement();
            String tag = root.getTagName();
            Message msg = new Message();

            switch (tag) {
                case "command" -> {
                    String name = root.getAttribute("name");
                    msg.setType(name);
                    msg.setName(getText(root, "name"));
                    String msgText = getText(root, "message");
                    if (msgText == null) msgText = getText(root, "password");
                    msg.setText(msgText);
                    msg.setSession(getText(root, "session"));
                    msg.setClientType(getText(root, "type"));
                }
                case "event" -> {
                    String name = root.getAttribute("name");
                    msg.setType("event_" + name);
                    msg.setName(getText(root, "name"));
                    msg.setText(getText(root, "message"));
                }
                case "success" -> {
                    msg.setType(Message.SUCCESS);
                    msg.setSession(getText(root, "session"));
                    NodeList userNodes = root.getElementsByTagName("user");
                    if (userNodes.getLength() > 0) {
                        msg.setType(Message.LIST_RESPONSE);
                        String[] users = new String[userNodes.getLength()];
                        for (int i = 0; i < userNodes.getLength(); i++) {
                            Element u = (Element) userNodes.item(i);
                            users[i] = getText(u, "name");
                        }
                        msg.setUsers(users);
                    }
                }
                case "error" -> {
                    msg.setType(Message.ERROR);
                    msg.setText(getText(root, "message"));
                }
            }
            return msg;
        } catch (Exception e) {
            throw new IOException("Error with parse XML: " + e.getMessage(), e);
        }
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }


    private String toXml(Message msg) throws IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();

            Element root = buildElement(doc, msg);
            doc.appendChild(root);

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

    private Element buildElement(Document doc, Message msg) {
        return switch (msg.getType()) {
            case Message.LOGIN -> {
                Element e = doc.createElement("command");
                e.setAttribute("name", "login");
                addChild(doc, e, "name", msg.getName());
                addChild(doc, e, "password", msg.getText());
                addChild(doc, e, "type", msg.getClientType() != null ? msg.getClientType() : "SwingClient");
                yield e;
            }
            case Message.LOGOUT -> {
                Element e = doc.createElement("command");
                e.setAttribute("name", "logout");
                addChild(doc, e, "session", msg.getSession());
                yield e;
            }
            case Message.MESSAGE -> {
                Element e = doc.createElement("command");
                e.setAttribute("name", "message");
                addChild(doc, e, "message", msg.getText());
                addChild(doc, e, "session", msg.getSession());
                yield e;
            }
            case Message.LIST -> {
                Element e = doc.createElement("command");
                e.setAttribute("name", "list");
                addChild(doc, e, "session", msg.getSession());
                yield e;
            }
            case Message.SUCCESS -> {
                Element e = doc.createElement("success");
                if (msg.getSession() != null) addChild(doc, e, "session", msg.getSession());
                yield e;
            }
            case Message.LIST_RESPONSE -> {
                Element e = doc.createElement("success");
                Element list = doc.createElement("listusers");
                if (msg.getUsers() != null) {
                    for (String u : msg.getUsers()) {
                        Element user = doc.createElement("user");
                        addChild(doc, user, "name", u);
                        addChild(doc, user, "type", "SwingClient");
                        list.appendChild(user);
                    }
                }
                e.appendChild(list);
                yield e;
            }
            case Message.ERROR -> {
                Element e = doc.createElement("error");
                addChild(doc, e, "message", msg.getText());
                yield e;
            }
            case Message.EVENT_MESSAGE -> {
                Element e = doc.createElement("event");
                e.setAttribute("name", "message");
                addChild(doc, e, "message", msg.getText());
                addChild(doc, e, "name", msg.getName());
                yield e;
            }
            case Message.EVENT_LOGIN -> {
                Element e = doc.createElement("event");
                e.setAttribute("name", "userlogin");
                addChild(doc, e, "name", msg.getName());
                yield e;
            }
            case Message.EVENT_LOGOUT -> {
                Element e = doc.createElement("event");
                e.setAttribute("name", "userlogout");
                addChild(doc, e, "name", msg.getName());
                yield e;
            }
            default -> doc.createElement("unknown");
        };
    }

    private void addChild(Document doc, Element parent, String tag, String text) {
        if (text == null) return;
        Element child = doc.createElement(tag);
        child.setTextContent(text);
        parent.appendChild(child);
    }
}
