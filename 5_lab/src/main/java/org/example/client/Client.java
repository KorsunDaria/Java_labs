package org.example.client;

import javax.swing.*;


public class Client {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new ChatWindow().setVisible(true);
        });
    }
}