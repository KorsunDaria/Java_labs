package org.example.model.entity;

import org.example.util.Vector2D;
import org.example.model.GameContext;

public class BossEnemy extends Enemy {

    private Player target;

    public BossEnemy(Player player) {
        this.width = 180; this.height = 250;
        this.tag = GameObjectTag.BOSS_ENEMY;
        this.target = player;
        this.combat = new CombatStats(100, 100, 10,
                0.5, 0.2);
        this.shapeType = "rectangle";
    }

    @Override
    public void update(double delta, GameContext context) {
        combat.update(delta);
        Vector2D pos = context.getOldPosition(this);
        Vector2D playerPos = context.getOldPosition(target);

        double distance = pos.distance(playerPos);

        if (distance < 300) {
            moveTowardsPlayer(context, target, delta);
        }
        if (combat.isDead()) {
            markForRemoval();
        }

    }

}
