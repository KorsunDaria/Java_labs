package org.example.model.entity;

import java.io.Serializable;

public class CombatStats implements Serializable {

    public int health;
    public int maxHealth;
    public int damage;

    public double attackCooldown;
    public double attackTimer;

    public double damageCooldown;
    public double damageTimer = 0;

    public double hitVisualTimer = 0;
    public double hitVisualDuration = 0.2;

    public CombatStats(int health, int maxHealth, int damage,
                       double attackCooldown,
                       double damageCooldown) {

        this.health = health;
        this.maxHealth = maxHealth;
        this.damage = damage;

        this.attackCooldown = attackCooldown;
        this.damageCooldown = damageCooldown;

        this.attackTimer = 0;
    }

    public void update(double delta) {

        if (attackTimer > 0) {
            attackTimer -= delta;
        }

        if (damageTimer > 0) {
            damageTimer -= delta;
        }

        if (hitVisualTimer > 0) {
            hitVisualTimer -= delta;
        }
    }

    public boolean canAttack() {
        return attackTimer <= 0;
    }

    public void triggerAttack() {
        attackTimer = attackCooldown;
    }

    public void takeDamage(int amount) {

        if (damageTimer > 0) return;

        health -= amount;

        if (health < 0) {
            health = 0;
        }

        damageTimer = damageCooldown;

        hitVisualTimer = hitVisualDuration;
    }

    public void heal(int hp) {
        health += hp;

        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean isInvulnerable() {
        return damageTimer > 0;
    }

    public boolean isHitVisualActive() {
        return hitVisualTimer > 0;
    }
}
