package org.example.client;

import org.example.protocol.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatWindow extends JFrame {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");


    private final CardLayout cards = new CardLayout();
    private final JPanel     root  = new JPanel(cards);

    private final JTextField     hostField   = new JTextField("localhost", 15);
    private final JTextField     portField   = new JTextField("12345", 6);
    private final JTextField     nickField   = new JTextField(15);
    private final JPasswordField passField   = new JPasswordField(15);
    private final JLabel         statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private       JButton        connectBtn;

    private final JTextArea                chatArea      = new JTextArea();
    private final JTextField               inputField    = new JTextField();
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String>            userList      = new JList<>(userListModel);


    private ServerConnection connection;
    private volatile boolean  loggedIn    = false;
    private          String   currentNick = "";

    public ChatWindow() {
        super("Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 580);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        root.add(buildLoginPanel(), "login");
        root.add(buildChatPanel(),  "chat");
        add(root);
        cards.show(root, "login");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendLogout();
                if (connection != null) connection.stop();
            }
        });
    }


    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Connect to server"),
                new EmptyBorder(10, 20, 10, 20)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill   = GridBagConstraints.HORIZONTAL;

        addFormRow(form, c, 0, "Host:",     hostField);
        addFormRow(form, c, 1, "Port:",     portField);
        addFormRow(form, c, 2, "Nickname:", nickField);
        addFormRow(form, c, 3, "Password:", passField);

        statusLabel.setForeground(Color.RED);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        form.add(statusLabel, c);

        connectBtn = new JButton("Connect");
        connectBtn.setFont(connectBtn.getFont().deriveFont(Font.BOLD));
        c.gridy = 5;
        form.add(connectBtn, c);

        connectBtn.addActionListener(e -> doConnect());

        KeyAdapter onEnter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doConnect();
            }
        };
        hostField.addKeyListener(onEnter);
        portField.addKeyListener(onEnter);
        nickField.addKeyListener(onEnter);
        passField.addKeyListener(onEnter);

        outer.add(form);
        return outer;
    }

    private void addFormRow(JPanel p, GridBagConstraints c, int row,
                            String labelText, JComponent field) {
        c.gridy = row; c.gridx = 0; c.gridwidth = 1; c.weightx = 0;
        p.add(new JLabel(labelText), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(field, c);
    }

    private JPanel buildChatPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel usersPanel = new JPanel(new BorderLayout(0, 4));
        usersPanel.setPreferredSize(new Dimension(150, 0));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setToolTipText("Update online users list");
        refreshBtn.addActionListener(e -> requestUserList());
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("Online"));
        usersPanel.add(refreshBtn, BorderLayout.NORTH);
        usersPanel.add(userScroll, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> doSend());
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doSend();
            }
        });

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendBtn,    BorderLayout.EAST);

        p.add(chatScroll, BorderLayout.CENTER);
        p.add(usersPanel, BorderLayout.EAST);
        p.add(inputRow,   BorderLayout.SOUTH);

        return p;
    }

    private void doConnect() {
        String host = hostField.getText().trim();
        String nick = nickField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            setLoginStatus("Port must be a number", true);
            return;
        }

        if (nick.isBlank()) { setLoginStatus("Nickname is required", true); return; }
        if (pass.isBlank()) { setLoginStatus("Password is required",  true); return; }

        connectBtn.setEnabled(false);
        setLoginStatus("Connecting...", false);

        currentNick = nick;
        loggedIn    = false;

        connection = new ServerConnection(host, port, nick, pass, this::handleIncoming);
        connection.start();
    }

    private void doSend() {
        if (connection == null) return;
        String text = inputField.getText().trim();
        if (text.isBlank()) return;

        try {
            connection.send(new Message(
                    Message.Type.CHAT, "", text, connection.getSessionId(), 0
            ));
            inputField.setText("");
        } catch (IOException e) {
            appendSystem("Send error: " + e.getMessage());
        }
    }

    private void requestUserList() {
        if (connection == null || connection.getSessionId().isEmpty()) return;
        try {
            connection.send(new Message(
                    Message.Type.LIST_REQUEST, "", "", connection.getSessionId(), 0
            ));
        } catch (IOException e) {
            appendSystem("List request failed: " + e.getMessage());
        }
    }

    private void sendLogout() {
        if (connection == null || connection.getSessionId().isEmpty()) return;
        try {
            connection.send(new Message(
                    Message.Type.LOGOUT, "", "", connection.getSessionId(), 0
            ));
        } catch (IOException ignored) {}
    }


    private void handleIncoming(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case SUCCESS       -> handleSuccess(msg);
                case ERROR         -> handleError(msg);
                case EVENT_MESSAGE -> appendChat(msg.getSender(), msg.getBody());
                case EVENT_LOGIN   -> {
                    appendSystem("→ " + msg.getSender() + " joined");
                    addUserToList(msg.getSender());
                }
                case EVENT_LOGOUT  -> {
                    appendSystem("← " + msg.getSender() + " left");
                    userListModel.removeElement(msg.getSender());
                }
                case LIST_RESPONSE          -> updateUserList(msg.getBody());
                default            -> {}
            }
        });
    }


    private void handleSuccess(Message msg) {
        if (!loggedIn) {
            loggedIn = true;
            cards.show(root, "chat");
            setTitle("Chat — " + currentNick);
            inputField.requestFocusInWindow();
            requestUserList();
        }
    }

    private void handleError(Message msg) {
        if (!loggedIn) {
            setLoginStatus("Error: " + msg.getBody(), true);
            connectBtn.setEnabled(true);
        } else {
            appendSystem("Server error: " + msg.getBody());
        }
    }


    private void appendChat(String sender, String text) {
        String time = LocalTime.now().format(TIME_FMT);
        chatArea.append("[" + time + "] " + sender + ": " + text + "\n");
        scrollToBottom();
    }

    private void appendSystem(String text) {
        String time = LocalTime.now().format(TIME_FMT);
        chatArea.append("[" + time + "] *** " + text + " ***\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateUserList(String csv) {
        userListModel.clear();
        if (csv == null || csv.isBlank()) return;
        for (String name : csv.split(",")) {
            String n = name.trim();
            if (!n.isEmpty()) userListModel.addElement(n);
        }
    }

    private void addUserToList(String name) {
        if (!userListModel.contains(name)) userListModel.addElement(name);
    }

    private void setLoginStatus(String text, boolean isError) {
        statusLabel.setForeground(isError ? Color.RED : Color.DARK_GRAY);
        statusLabel.setText(text);
    }
}