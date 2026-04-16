package org.example.model.entity;

import org.example.model.GameContext;

public class SimpleEnemy extends Enemy {

    private Player target;

    public SimpleEnemy(Player player) {
        this.target = player;
        this.shapeType = "ellipse";
        this.width = 80; this.height = 80;
        this.tag = GameObjectTag.SIMPLE_ENEMY;
        this.combat = new CombatStats(20, 50, 10, 1.0, 0.3);
    }

    @Override
    public void update(double delta, GameContext context) {
        combat.update(delta);
        moveTowardsPlayer(context, target, delta);
        if (combat.isDead()) {
            markForRemoval();
        }
    }


}
