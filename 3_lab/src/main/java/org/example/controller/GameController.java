package org.example.controller;

import org.example.model.GameContext;
import org.example.model.GameModel;
import org.example.model.GameState;
import org.example.model.entity.Player;
import org.example.model.level.*;
import org.example.util.SaveManager;
import org.example.util.StatsManager;

public class GameController {

    private final GameModel model;
    private final GameLoop gameLoop;
    private final KeyboardHandler keyboardHandler;

    private Player player;
    private Level currentLevel;

    public GameController(GameModel model) {
        this.model = model;
        this.gameLoop = new GameLoop(model);
        this.keyboardHandler = new KeyboardHandler(model, this);
    }

    public KeyboardHandler getKeyboardHandler() {
        return keyboardHandler;
    }

    public void update(double delta) {
        if (model.getGameState() == GameState.PLAYING) {
            keyboardHandler.applyInputToPlayer();
            gameLoop.update(delta);
            checkLevelConditions();
        }
    }

    public void loadLevel(int levelId) {
        keyboardHandler.reset();

        GameContext context = SaveManager.loadGame(model.getPlayerName(), levelId);
        boolean isNewGame = (context == null);

        if (isNewGame) {
            context = new GameContext();
        }
        model.setCurrentContext(context);

        player = (Player) context.getAllObjects().stream()
                .filter(obj -> obj instanceof Player)
                .findFirst()
                .orElse(null);

        if (player == null) {
            player = new Player();
            context.addObject(player, 0 , 0);
        }

        keyboardHandler.setActivePlayer(player);

        currentLevel = model.createLevelInstance(levelId, context);

        if (isNewGame) {
            LevelGenerator.generateLevel(currentLevel, player);
        }

        model.setCurrentLevelId(levelId);
        model.setGameState(GameState.PLAYING);
    }

    private void checkLevelConditions() {
        if (player.getCombat().isDead()) {
            model.setGameState(GameState.LOSE);
            deleteSaveFile(currentLevel.getId());
            return;
        }

        if (currentLevel != null && currentLevel.isCompleted()) {
            model.setGameState(GameState.WIN);
            model.unlockLevel(currentLevel.getId() + 1);
            int id = currentLevel.getId();
            StatsManager.updateStats(model, id );

            deleteSaveFile(id);
        }
    }

    private void deleteSaveFile(int levelId) {
        java.io.File file = new java.io.File("saves/" + model.getPlayerName() + "_lvl" + levelId + ".ser");
        if (file.exists()) {
            file.delete();
        }
    }
}