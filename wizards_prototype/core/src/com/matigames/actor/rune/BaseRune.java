package com.matigames.actor.rune;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.matigames.actor.BaseActor;

public class BaseRune extends BaseActor {

    public BaseRune(float x, float y, Stage s, String[] files, String hint, Skin skin) {
        super(x, y, s);

        loadAnimationFromFiles(files, 0.05f, true);

        TextTooltip tooltip = new TextTooltip(hint, skin);
        tooltip.setInstant(true);

        this.addListener(tooltip);
    }
}
