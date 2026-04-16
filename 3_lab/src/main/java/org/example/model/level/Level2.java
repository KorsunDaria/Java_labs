package org.example.model.level;

import org.example.model.GameContext;
import org.example.model.entity.GameObject;
import org.example.model.entity.GameObjectTag;
import org.example.model.level.Level;
import org.example.model.level.LevelConfig;

public class Level2 extends Level {

    public Level2(GameContext context) {
        super(2, context);
    }

    @Override
    protected LevelConfig createConfig() {

        LevelConfig config = new LevelConfig();

        config.setPlayerStartPos(0, 0);

        config.addEntity("FRUIT", 7);
        config.addEntity("SIMPLE_ENEMY", 5);
        config.addEntity("BUG_ENEMY", 5);
        config.addEntity("WALL", 3);

        return config;
    }

    @Override
    public boolean isCompleted() {

        boolean hasFruit = false;
        boolean hasEnemy = false;

        for (GameObject obj : context.getAllObjects()) {

            if (obj.getTag() == GameObjectTag.FRUIT) {
                hasFruit = true;
            }

            if (obj.getTag() == GameObjectTag.SIMPLE_ENEMY ||
                    obj.getTag() == GameObjectTag.BUG_ENEMY ||
                    obj.getTag() == GameObjectTag.ENEMY) {
                hasEnemy = true;
            }
        }

        return !hasFruit && !hasEnemy;
    }
}
