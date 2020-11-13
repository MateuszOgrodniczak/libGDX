package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.spell.SpellSchool;

public class Frostbolt extends EnemySpell {
    public Frostbolt(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/IcePick_64x64.png", 1, 30, 0.1f, true);
        setBoundaryPolygon(3, 1, 1);
        scaleBy(1.5f);

        addAction(Actions.delay(3));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(450);
        setMaxSpeed(450);
        setDeceleration(0);
        school = SpellSchool.ICE;

        int angleChange = randomAngle.nextInt(20) - 10;
        setRotation(-90 + angleChange);
        setMotionAngle(-90 + angleChange);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();
    }
}
