package org.example.model.level;

import org.example.model.GameContext;
import org.example.model.entity.GameObject;
import org.example.model.entity.GameObjectTag;

public class Level3 extends Level {

    public Level3(GameContext context) {
        super(3, context);
    }

    @Override
    protected LevelConfig createConfig() {

        LevelConfig config = new LevelConfig();

        config.setPlayerStartPos(600, -200);

        config.addEntity("FRUIT", 7);
        config.addEntity("SIMPLE_ENEMY", 5);
        config.addEntity("BUG_ENEMY", 5);
        config.addEntity("BOSS_ENEMY", 1);
        config.addEntity("WALL", 3);

        return config;
    }

    @Override
    public boolean isCompleted() {

        boolean bossAlive = false;
        boolean fruitLeft = false;

        for (GameObject obj : context.getAllObjects()) {

            if (obj.getTag() == GameObjectTag.BOSS_ENEMY) {
                bossAlive = true;
            }

            if (obj.getTag() == GameObjectTag.FRUIT) {
                fruitLeft = true;
            }
        }

        return !bossAlive && !fruitLeft;
    }
}
