package chat.client;

import javax.swing.*;
import java.awt.*;


public class LoginDialog extends JDialog {

    private final boolean useXml;

    private final JTextField     hostField     = new JTextField("localhost", 15);
    private final JTextField     portField     = new JTextField("12345", 6);
    private final JTextField     nameField     = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel         errorLabel    = new JLabel(" ");
    private final JButton        loginBtn      = new JButton("Войти");


    public LoginDialog(boolean useXml) {
        this.useXml = useXml;
        setTitle("Вход в чат (" + (useXml ? "XML" : "Serial") + ")");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        addRow(panel, gc, 0, "Сервер:", hostField);
        addRow(panel, gc, 1, "Порт:",   portField);
        addRow(panel, gc, 2, "Ник:",    nameField);
        addRow(panel, gc, 3, "Пароль:", passwordField);

        errorLabel.setForeground(Color.RED);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        panel.add(errorLabel, gc);

        loginBtn.addActionListener(e -> onLogin());
        getRootPane().setDefaultButton(loginBtn);
        gc.gridy = 5; gc.anchor = GridBagConstraints.CENTER;
        panel.add(loginBtn, gc);

        add(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = row;
        panel.add(new JLabel(label), gc);
        gc.gridx = 1;
        panel.add(field, gc);
    }

    private void onLogin() {
        String host     = hostField.getText().trim();
        String portText = portField.getText().trim();
        String name     = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (host.isEmpty() || name.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            showError("Некорректный порт");
            return;
        }

        loginBtn.setEnabled(false);
        showError("Подключение...");

        Connection connection = new Connection(host, port, useXml, name, password);

            try {
                connection.tryLogin();


                ChatWindow chatWindow = new ChatWindow(name, connection);
                connection.startReading(chatWindow);
                chatWindow.setVisible(true);
                dispose();


            } catch (Connection.AuthException e) {
                showError(e.getMessage());
                loginBtn.setEnabled(true);


            } catch (Exception e) {
                showError("Не удалось подключиться: " + e.getMessage());
                loginBtn.setEnabled(true);
            }

    }

    private void showError(String text) {
        errorLabel.setText(text);
    }
}
