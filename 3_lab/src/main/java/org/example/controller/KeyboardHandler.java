package org.example.controller;

import org.example.util.SaveManager;
import org.example.util.Vector2D;
import org.example.model.GameModel;
import org.example.model.GameState;
import org.example.model.entity.Player;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardHandler extends KeyAdapter {

    private final GameModel model;
    private final GameController mainController;
    private Player activePlayer;

    private boolean up, down, left, right;

    public KeyboardHandler(GameModel model, GameController mainController) {
        this.model = model;
        this.mainController = mainController;
    }

    public void setActivePlayer(Player player) {
        this.activePlayer = player;
    }

    public void applyInputToPlayer() {
        if (activePlayer == null || activePlayer.getCombat().isDead()) return;

        double dx = 0;
        double dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        activePlayer.getMovement().setDirection(new Vector2D(dx, dy));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        GameState state = model.getGameState();

        switch (state) {
            case MENU -> {
                if (key == KeyEvent.VK_ENTER) model.setGameState(GameState.PLANET_SELECT);
                if (key == KeyEvent.VK_C) model.setGameState(GameState.STATISTIC);
            }
            case PLANET_SELECT -> {
                if (key == KeyEvent.VK_A) model.decreaseCurrentLevelId();
                if (key == KeyEvent.VK_D) model.increaseCurrentLevelId();

                if (key == KeyEvent.VK_ENTER) {
                    System.out.println(model.getCurrentLevelId());
                    if (model.isLevelUnlocked(model.getCurrentLevelId())) {
                        mainController.loadLevel(model.getCurrentLevelId());
                    }
                }
                if (key == KeyEvent.VK_ESCAPE) model.setGameState(GameState.MENU);
            }
            case PLAYING -> {
                if (key == KeyEvent.VK_W) up = true;
                if (key == KeyEvent.VK_S) down = true;
                if (key == KeyEvent.VK_A) left = true;
                if (key == KeyEvent.VK_D) right = true;

                if (e.getKeyCode() == KeyEvent.VK_J && activePlayer != null) {
                        activePlayer.setAttacking(true);
                }

                if (key == KeyEvent.VK_ESCAPE) {
                    model.setGameState(GameState.PAUSED);
                }
            }
            case PAUSED -> {
                if (key == KeyEvent.VK_ESCAPE) model.setGameState(GameState.PLAYING);
                if (key == KeyEvent.VK_Q) {
                    SaveManager.saveGame(model);
                    model.setGameState(GameState.MENU);
                }
            }
            case WIN -> {
                if (key == KeyEvent.VK_ENTER) model.setGameState(GameState.PLANET_SELECT);
            }
            case LOSE -> {
                if (key == KeyEvent.VK_ENTER) model.setGameState(GameState.PLANET_SELECT);
            }
            case STATISTIC -> {
                if (key == KeyEvent.VK_ENTER) model.setGameState(GameState.MENU);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (model.getGameState() != GameState.PLAYING) return;

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_D) right = false;
        if (e.getKeyCode() == KeyEvent.VK_J && activePlayer != null) {
            activePlayer.setAttacking(false);
        }
    }

    public void reset() {
        up= false;
        down = false;
        left = false;
        right= false;
    }
}