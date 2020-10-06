package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class FireMissile extends EnemySpell {

    public FireMissile(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/fireball_1_64x64.png", 1, 60, 0.025f, true);
        setBoundaryPolygon(3, 1, 1);
        scaleBy(1.5f);

        addAction(Actions.delay(3));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(400);
        setMaxSpeed(400);
        setDeceleration(0);

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
