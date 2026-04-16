package org.example.model;

import org.example.model.GameContext;
import org.example.model.GameState;
import org.example.model.level.*;
import org.example.model.level.Level1;
import org.example.model.level.Level3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GameModel {

    private GameState gameState = GameState.MENU;
    private GameContext currentContext;
    private int currentLevelId = 1;

    private Set<Integer> unlockedLevels = new HashSet<>(Set.of(1));

    private final List<Function<GameContext, Level>> levelFactories = List.of(
            Level1::new, // Это ссылка на конструктор: берет context, возвращает Level1
            Level2::new,
            Level3::new
    );


    private String playerName = "Player1";;

    private int score;

    public GameState getGameState() {return gameState;}
    public void setGameState(GameState gameState) {this.gameState = gameState;}

    public GameContext getCurrentContext() { return currentContext;}
    public void setCurrentContext(GameContext currentContext) { this.currentContext = currentContext;}


    public int getCurrentLevelId() { return currentLevelId;}

    public void setCurrentLevelId(int currentLevelId) {this.currentLevelId = currentLevelId;}

    public Set<Integer> getUnlockedLevels() {return unlockedLevels;}
    public void unlockLevel(int levelId) {unlockedLevels.add(levelId);}
    public boolean isLevelUnlocked(int levelId) {return unlockedLevels.contains(levelId);}


    public String getPlayerName() {return playerName;}
    public void setPlayerName(String playerName) {this.playerName = playerName;}


    public int getScore() {return score;}
    public void setScore(int score) {this.score = score;}
    public void addScore(int value) {this.score += value;}

    public void increaseCurrentLevelId() {
        if (currentLevelId < 3) currentLevelId++;
    }
    public void decreaseCurrentLevelId() {
        if (currentLevelId > 1) currentLevelId--;
    }

    public int getTotalLevels() {
        return levelFactories.size();
    }

    public Level createLevelInstance(int levelId, GameContext context) {
        int index = levelId - 1;
        if (index < 0 || index >= levelFactories.size()) {
            throw new IllegalArgumentException("Unknown level: " + levelId);
        }
        return levelFactories.get(index).apply(context);
    }
}
