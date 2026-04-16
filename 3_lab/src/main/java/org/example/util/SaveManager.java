package org.example.util;

import org.example.model.GameContext;
import org.example.model.GameModel;

import java.io.*;
import java.nio.file.*;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    public static void saveGame(GameModel model) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            String fileName = String.format("%s_lvl%d.ser", model.getPlayerName(), model.getCurrentLevelId());

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_DIR + fileName))) {
                oos.writeObject(model.getCurrentContext());
                System.out.println("Игра сохранена: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameContext loadGame(String playerName, int levelId) {
        String fileName = String.format("%s_lvl%d.ser", playerName, levelId);
        File file = new File(SAVE_DIR + fileName);

        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameContext) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}