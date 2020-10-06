package com.matigames.actor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class Explosion extends BaseActor {
    public Explosion(float x, float y, Stage s, byte type, int frames) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/Explosion_" + type + "_96x96.png", 1, frames, 0.05f, true);

        scaleBy(1.5f);

        addAction(Actions.delay(1));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
    }
}
