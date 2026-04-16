package org.example.model.entity;

import org.example.util.Vector2D;
import org.example.model.GameContext;
import org.example.util.ShapeUtils;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Player extends GameObject {
    private boolean isAttackingState = false;
    private Vector2D lastDirection = new Vector2D(1, 0);

    public Player() {
        this.tag = GameObjectTag.PLAYER;
        this.width = 130;
        this.height = 120;
        this.shapeType = "ellipse";
        this.movement = new MovementStats(600);
        this.combat = new CombatStats(100, 100, 10, 0.1, 0.2);
    }

    @Override
    public void update(double delta, GameContext context) {
        combat.update(delta);
        Vector2D pos = context.getOldPosition(this);
        Vector2D direction = movement.getDirection();

        Vector2D newPos = pos.add(direction.scale(movement.getSpeed() * delta));
        double x = Math.max(-650, Math.min(newPos.x(), 760 - width));
        double y = Math.max(-430, Math.min( newPos.y(), 450 - height));

        context.setNewPosition(this, new Vector2D(x,y));

        Vector2D moveDelta = newPos.subtract(pos);
        if (moveDelta.length() > 0.001) {
            lastDirection = moveDelta.normalize();
        }

        if (combat.isDead()) {
            markForRemoval();
        }

    }

    @Override
    public void onCollision(GameObject other, GameContext context) {
        if (other instanceof Wall) {
            context.rollback(this);
        }
        if (other instanceof Enemy && isAttackingState) {
            attack(other, context);
        }
    }

    public void attack(GameObject target, GameContext context) {
        if (!combat.canAttack()) return;

        Shape attackHitbox = createAttackShape(context);

        Vector2D targetPos = context.getNewPosition(target);
        Shape targetShape = ShapeUtils.createShape(
                target.getShapeType(),
                targetPos.x(),
                targetPos.y(),
                target.getWidth(),
                target.getHeight()
        );

        java.awt.geom.Area areaA = new java.awt.geom.Area(attackHitbox);
        java.awt.geom.Area areaB = new java.awt.geom.Area(targetShape);
        areaA.intersect(areaB);

        if (!areaA.isEmpty()) {
            target.getCombat().takeDamage(this.combat.damage);
            combat.triggerAttack();
        }
    }

    private Shape createAttackShape(GameContext context) {
        Vector2D pos = context.getNewPosition(this);

        double attackWidth = 200;
        double attackRange = 80;

        Rectangle2D.Double hitRect = new Rectangle2D.Double(
                pos.x() + this.width / 2,
                pos.y() + this.height / 2 - attackWidth / 2,
                attackRange,
                attackWidth
        );

        double angle = Math.atan2(lastDirection.y(), lastDirection.x());

        AffineTransform at = AffineTransform.getRotateInstance(
                angle,
                pos.x() + this.width / 2,
                pos.y() + this.height / 2
        );

        return at.createTransformedShape(hitRect);
    }

    public boolean isAttacking(){
        return isAttackingState;
    }
    public void setAttacking(boolean q) {
        isAttackingState = q;
    }

    public Vector2D getLastDirection() {
        return lastDirection;
    }
}