package org.example.model.entity;

import org.example.util.Vector2D;

import java.io.Serializable;

public class MovementStats implements Serializable {

    private double speed;
    private Vector2D direction = new Vector2D(0, 0);

    public MovementStats(double speed) {
        this.speed = speed;
    }

    public void setDirection(Vector2D direction) {
        this.direction = direction.normalize();
    }
    public Vector2D getDirection() {
        return direction;
    }

    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Vector2D computeVelocity(double deltaTime) {
        return direction.scale(speed * deltaTime);
    }
}
