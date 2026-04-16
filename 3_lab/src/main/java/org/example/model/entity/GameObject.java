package org.example.model.entity;

import org.example.model.GameContext;

import java.io.Serializable;

public abstract class GameObject implements Serializable {

    protected double width;
    protected double height;

    protected boolean isStatic;
    protected boolean pendingRemoval;
    protected String shapeType = "rectangle";

    protected GameObjectTag tag;

    protected MovementStats movement;
    protected CombatStats combat;

    public abstract void update(double delta, GameContext context);

    public abstract void onCollision(GameObject other, GameContext context);

    public void markForRemoval() {pendingRemoval = true;}

    public boolean isPendingRemoval() {return pendingRemoval;}

    public GameObjectTag getTag() {return tag;}
    public double getWidth(){return width;}

    public double getHeight() {return height;}
    public String getShapeType(){return shapeType;}

    public CombatStats getCombat() {
        return combat;
    };
    public MovementStats getMovement() {
        return movement;
    };
}
