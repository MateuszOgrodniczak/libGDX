package com.matigames.actor;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class Arrow extends BaseActor {
    public Arrow(float x, float y, Stage s, String texture) {
        super(x, y, s);

        loadTexture(texture);
    }
}
