package org.example.model.entity;

import org.example.util.Vector2D;
import org.example.model.GameContext;

public abstract class Enemy extends GameObject {

    public Enemy() {
        this.movement = new MovementStats(80);
    }

    protected void moveTowardsPlayer(GameContext context, Player player, double delta) {
        Vector2D pos = context.getOldPosition(this);
        Vector2D playerPos = context.getNewPosition(player);

        Vector2D dir = playerPos.subtract(pos).normalize();

        Vector2D newPos = pos.add(dir.scale(movement.getSpeed() * delta));

        context.setNewPosition(this, newPos);
    }

    @Override
    public void onCollision(GameObject other, GameContext context) {
        if (other instanceof Player) {
            attack(other);
        }
    }

    public CombatStats getCombat() {
        return combat;
    }

    public void attack(GameObject obj) {

        if (!(obj instanceof Player player)) return;

        if (!combat.canAttack()) return;

        player.getCombat().takeDamage(combat.damage);

        combat.triggerAttack();
    }

}
