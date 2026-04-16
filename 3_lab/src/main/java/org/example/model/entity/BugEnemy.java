package org.example.model.entity;

import org.example.util.Vector2D;
import org.example.model.GameContext;

public class BugEnemy extends Enemy {

    private double time = 0;
    private Player target;

    public BugEnemy(Player player) {
        this.width = 120; this.height = 100;
        this.tag = GameObjectTag.BUG_ENEMY;
        this.target = player;
        this.combat = new CombatStats(10, 100, 10,
                0.5, 0.2);
        this.shapeType = "ellipse";
    }

    @Override
    public void update(double delta, GameContext context) {
        time += delta;
        combat.update(delta);

        Vector2D pos = context.getOldPosition(this);
        Vector2D playerPos = context.getOldPosition(target);

        Vector2D dir = playerPos.subtract(pos).normalize();

        double offset = Math.sin(time * 5) * 50;

        Vector2D side = new Vector2D(-dir.y(), dir.x()).scale(offset);

        Vector2D newPos = pos.add(dir.scale(movement.getSpeed() * delta)).add(side.scale(delta));

        context.setNewPosition(this, newPos);
        if (combat.isDead()) {
            markForRemoval();
        }
    }

}
