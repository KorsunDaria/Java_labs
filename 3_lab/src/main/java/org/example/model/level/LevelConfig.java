package org.example.model.level;

import java.util.HashMap;
import java.util.Map;

public class LevelConfig {

    public double playerX, playerY;

    public Map<String, Integer> counts = new HashMap<>();

    public void setPlayerStartPos(double x, double y) {
        this.playerX = x;
        this.playerY = y;
    }

    public void addEntity(String type, int count) {
        counts.put(type, count);
    }

    public int getCount(String type) {
        return counts.getOrDefault(type, 0);
    }
}
