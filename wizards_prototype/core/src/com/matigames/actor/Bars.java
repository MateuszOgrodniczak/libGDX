package com.matigames.actor;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class Bars extends BaseActor {

    public Bars(float x, float y, Stage s, String name) {
        super(x, y, s);

        loadTexture("assets/bars/" + name + ".png");
    }
}
