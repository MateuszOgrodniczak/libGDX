package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class MassiveFireMissile extends EnemySpell {
    public MassiveFireMissile(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/fireball_3_64x64.png", 1, 45, 0.025f, true);
        setBoundaryPolygon(3, 1, 1);
        scaleBy(5);

        addAction(Actions.delay(5));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(125);
        setMaxSpeed(125);
        setDeceleration(0);

        int angleChange = randomAngle.nextInt(5) - 5;
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
