package chat.client;

import javax.swing.*;


public class Main {

    public static void main(String[] args) {
        boolean useXml = true;
        for (String arg : args) {
            if ("--serial".equals(arg)) useXml = false;
            if ("--xml".equals(arg))    useXml = true;
        }

        final boolean xml = useXml;

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(xml);
            login.setVisible(true);
        });
    }
}
