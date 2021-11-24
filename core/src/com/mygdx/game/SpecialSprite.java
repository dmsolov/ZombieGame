package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public abstract class SpecialSprite{

    protected final Vector2 location;
    protected final Vector2 velocity;
    protected final Vector2 acceleration;

    protected final float maxForce = 1f;
    protected final float maxSpeed = 1.7f;

    protected float centerX;
    protected float centerY;


    public SpecialSprite(Vector2 location) {

        this.location = location;
        this.velocity = new Vector2(0, 0);
        this.acceleration = new Vector2(0, 0);
    }

    protected void applyForce(Vector2 force) {
        acceleration.add(force);
    }

    protected void move(){

        // скорость
        velocity.add(acceleration);

        // макс скорость
        velocity.limit(maxSpeed);

        // изменяет цель
        location.add(velocity);

        // ускорение
        acceleration.scl(0);

    }

    protected void seek(Vector2 target) {

        Vector2 desired = target.cpy();
        desired.sub(location);

        // len2 - расстояние от цели до цели

        float d = desired.len2();
        desired.nor();

        if (d < 100f) {
            desired.scl(0);
        }
        else {
            desired.scl(maxSpeed);
        }

        Vector2 steer = desired.cpy();
        steer.sub(velocity);
        steer.limit(maxForce);

        applyForce(steer);

    }

    /**
     * Update node position
     */
    protected abstract void display();

    public Vector2 getLocation() {
        return location;
    }

    public void update(Vector2 v){
        seek(v);
        move();
        display();
    }
}
