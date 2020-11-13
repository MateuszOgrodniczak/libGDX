package com.matigames.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class DamageText extends Label {
    private static final float scaleFactor = 1.5f;
    private Direction direction;

    public DamageText(CharSequence text, Skin skin, Direction direction, boolean isCrit) {
        super(text, skin);

        this.direction = direction;
        setColor(Color.YELLOW);

        setFontScale(isCrit ? scaleFactor * 2 : scaleFactor);

        addAction(Actions.delay(1));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
    }

    @Override
    public void act(float dt) {
        super.act(dt);
       /* if(getY() >= Gdx.graphics.getHeight() * 0.85) {
            peakReached = true;
        }
        if(peakReached) {
            moveBy(-0.5f, -1.5f);
        }
        else {
            moveBy(-0.5f, 1.5f);
        }*/
        if (direction == Direction.LEFT) {
            moveBy(-1f, 1f);
        } else {
            moveBy(1f, 1f);
        }
    }
}
