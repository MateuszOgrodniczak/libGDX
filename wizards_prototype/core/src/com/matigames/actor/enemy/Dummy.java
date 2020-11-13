package com.matigames.actor.enemy;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class Dummy extends BaseEnemy {

    public Dummy(float x, float y, Stage s) {
        super(x, y, s);

        loadTexture("assets/dummy.png");

        setName("Training dummy");
        baseHP = 100;
        hp = baseHP;
        immortal = true;

        setWidth(200);
        setHeight(200);
        setBoundaryPolygon(4, 1, 1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();
    }
}
