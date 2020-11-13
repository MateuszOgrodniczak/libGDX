package com.matigames.actor.spell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.Player;

public class SpellDeflection extends Spell {
    private Actor caster;

    public SpellDeflection(float x, float y, Stage s, Player caster) {
        super(x, y, s);

        this.caster = caster;
        adjustPosition();

        String[] animations = new String[20];
        for(int i = 1; i<30; i++) {
            String frameNumber = i <= 9 ? ("0" + i) : ""+i;
            animations[i-1] = "assets/wind/medium/windMedium00" + frameNumber + ".png";
        }
        loadAnimationFromFiles(animations, 0.1f, true);

        addAction(Actions.delay(4));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        adjustPosition();
    }

    private void adjustPosition() {
        float x = caster.getX() + caster.getWidth()/2 - 25;
        float y = caster.getY() + caster.getHeight()/2;
        setPosition(x, y);
    }
}
