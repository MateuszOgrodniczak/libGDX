package com.matigames.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;

public abstract class BaseGame extends Game {

    @Override
    public void create() {
        System.out.println("GDX WIDTH: " + Gdx.graphics.getWidth() + ", H: " + Gdx.graphics.getHeight());
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        // System.out.println("DELTA: " + delta);
        update(delta);
    }

    public abstract void update(float delta);
}
