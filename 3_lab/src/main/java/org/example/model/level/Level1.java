package org.example.model.level;

import org.example.model.GameContext;
import org.example.model.entity.GameObject;
import org.example.model.entity.GameObjectTag;

public class Level1 extends Level {

    public Level1(GameContext context) {
        super(1, context);
    }

    @Override
    protected LevelConfig createConfig() {

        LevelConfig config = new LevelConfig();

        config.setPlayerStartPos(-600, 200);

        config.addEntity("FRUIT", 8);
        config.addEntity("WALL", 2);

        return config;
    }

    @Override
    public boolean isCompleted() {

        for (GameObject obj : context.getAllObjects()) {
            if (obj.getTag() == GameObjectTag.FRUIT) {
                return false;
            }
        }

        return true;
    }
}
