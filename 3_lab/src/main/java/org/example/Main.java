package org.example;

import org.example.controller.GameController;
import org.example.model.GameModel;
import org.example.util.StatsManager; // Убедись, что путь верный
import org.example.view.ConsoleView;
import org.example.view.GamePanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        String name = JOptionPane.showInputDialog(null, "Введите имя игрока:", "Вход", JOptionPane.QUESTION_MESSAGE);

        if (name == null) {
            System.exit(0);
        }
        if (name.trim().isEmpty()) {
            name = "Player1";
        }


        GameModel model = new GameModel();
        model.setPlayerName(name);

        StatsManager.loadProgressForPlayer(model);

        GameController controller = new GameController(model);


        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bikini Bottom Action - Игрок: " + model.getPlayerName());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            GamePanel panel = new GamePanel(model, controller);
            frame.add(panel);
            frame.setVisible(true);
        });


        ConsoleView console = new ConsoleView();
        console.start();
    }
}