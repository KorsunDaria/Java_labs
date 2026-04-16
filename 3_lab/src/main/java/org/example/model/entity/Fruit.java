package org.example.model.entity;

import org.example.model.GameContext;

public class Fruit extends GameObject {
    int healHp;

    public Fruit(int healHp) {
        this.tag = GameObjectTag.FRUIT;
        this.healHp = healHp;
        this.shapeType = "ellipse";
        this.width = 60; this.height = 60;
    }

    @Override
    public void update(double delta, GameContext context) {}

    @Override
    public void onCollision(GameObject other, GameContext context) {
        if (other instanceof Player) {
            other.getCombat().heal(healHp);
            markForRemoval();
        }
    }
}
