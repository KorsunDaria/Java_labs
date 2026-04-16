package org.example.model.entity;

import org.example.model.GameContext;
import org.example.util.Vector2D;

public class Wall extends GameObject {

    public Wall() {
        this.tag = GameObjectTag.WALL;
        this.isStatic = true;
        this.shapeType = "ellipse";
        this.width = 100; this.height = 100;
    }

    @Override
    public void update(double delta, GameContext context) {
    }

    @Override
    public void onCollision(GameObject other, GameContext context) {
        context.rollback(other);
    }

}
