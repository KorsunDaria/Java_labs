package chat.client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ChatWindow extends JFrame {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final String     myName;
    private final Connection connection;

    private final JTextPane  chatArea    = new JTextPane();
    private final JList<String> userList = new JList<>(new DefaultListModel<>());
    private final JTextField inputField  = new JTextField();
    private final JLabel     statusLabel = new JLabel("Сonnecting...");

    public ChatWindow(String myName, Connection connection) {
        this.myName     = myName;
        this.connection = connection;

        setTitle("Чат — " + myName);
        setSize(700, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                connection.disconnect();
                dispose();
            }
        });

        buildUI();
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane chatScroll = new JScrollPane(chatArea);

        userList.setPreferredSize(new Dimension(140, 0));
        userList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("Members"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, userScroll);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(4);

        inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        inputField.addActionListener(e -> sendMessage());

        JButton sendBtn  = new JButton("Send");
        sendBtn.addActionListener(e -> sendMessage());

        JButton listBtn  = new JButton("Refresh Members List");
        listBtn.addActionListener(e -> connection.requestUserList());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.add(sendBtn);
        btnPanel.add(listBtn);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        add(splitPane,   BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.setText("");
        connection.sendMessage(text);
    }


    public void appendMessage(String fromName, String text) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            boolean isMe = myName.equals(fromName);

            StyledDocument doc = chatArea.getStyledDocument();
            Style base = chatArea.getLogicalStyle();


            appendStyled(doc, "[" + time + "] ", Color.GRAY, false);

            Color nameColor = isMe ? new Color(0, 100, 200) : new Color(150, 0, 0);
            appendStyled(doc, fromName + ": ", nameColor, true);

            appendStyled(doc, text + "\n", Color.BLACK, false);

            scrollToBottom();
        });
    }

    public void appendEvent(String eventText) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            appendStyled(doc, "*** " + eventText + " ***\n", new Color(100, 100, 100), false);
            scrollToBottom();
        });
    }


    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) userList.getModel();
            model.clear();
            if (users != null) {
                for (String u : users) model.addElement(u);
            }
        });
    }


    public void showStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }


    public void showAuthError(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    reason,
                    "Entrance error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();

            LoginDialog login = new LoginDialog(connection.isXml());
            login.setVisible(true);
        });
    }

    private void appendStyled(StyledDocument doc, String text, Color color, boolean bold) {
        Style style = doc.addStyle("s", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
    }

    private void scrollToBottom() {
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
