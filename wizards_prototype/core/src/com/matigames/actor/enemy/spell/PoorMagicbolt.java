package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.spell.SpellSchool;

public class PoorMagicbolt extends EnemySpell {
    public PoorMagicbolt(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/IcePick_64x64.png", 1, 60, 0.02f, true);
        setBoundaryPolygon(3, 1, 1);
        scaleBy(0.75f);

        addAction(Actions.delay(3));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(300);
        setMaxSpeed(300);
        setDeceleration(0);
        school = SpellSchool.ARCANE;

        setColor(0.4f, 0.1f, 0.8f, 1);

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
