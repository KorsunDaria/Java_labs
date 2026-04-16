package org.example.controller;

import org.example.util.Vector2D;
import org.example.model.GameContext;
import org.example.model.entity.*;
import org.example.model.level.Level;
import org.example.model.level.LevelConfig;

import java.util.Map;
import java.util.Random;

public class LevelGenerator {

    private static final double MIN_X = -650;
    private static final double MAX_X = 650;
    private static final double MIN_Y = -300;
    private static final double MAX_Y = 300;
    private static final double SAFE_DISTANCE = 130.0;

    public static void generateLevel(Level level, Player player) {
        GameContext context = level.getContext();
        LevelConfig config = level.getConfig();
        Random rand = new Random();

        context.addObject(player, config.playerX, config.playerY);

        for (Map.Entry<String, Integer> entry : config.counts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                GameObject obj = createEntity(type, player);
                if (obj != null) {
                    Vector2D pos = findSafePosition(context, rand);
                    context.addObject(obj, pos.x(), pos.y());
                }
            }
        }
    }

    private static Vector2D findSafePosition(GameContext context, Random rand) {
        for (int i = 0; i < 50; i++) {
            double x = MIN_X + (MAX_X - MIN_X) * rand.nextDouble();
            double y = MIN_Y + (MAX_Y - MIN_Y) * rand.nextDouble();
            Vector2D candidate = new Vector2D(x, y);

            boolean isSafe = true;
            for (GameObject obj : context.getAllObjects()) {
                if (context.getNewPosition(obj).distance(candidate) < SAFE_DISTANCE) {
                    isSafe = false;
                    break;
                }
            }
            if (isSafe) return candidate;
        }
        return new Vector2D(0, 0);
    }

    private static GameObject createEntity(String type, Player player) {
        return switch (type) {
            case "FRUIT" -> new Fruit(10);
            case "SIMPLE_ENEMY" -> new SimpleEnemy(player);
            case "BUG_ENEMY" -> new BugEnemy(player);
            case "BOSS_ENEMY" -> new BossEnemy(player);
            case "WALL" -> new Wall();
            default -> null;
        };
    }


}