package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class FrostNova extends Spell {
    public static String directionsRegExp = "(L|U)+(R|U)+(R|D)+(L|D)+";

    public FrostNova(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/IceCast_96x96.png", 1, 28, 0.5f, false);

        baseDmg = 800;
        school = SpellSchool.ICE;

        scaleBy(3f);
        addAction(Actions.delay(3));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(150);
        setMaxSpeed(150);
        setDeceleration(0);

        setRotation(90);
        setMotionAngle(90);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();
    }

    public static boolean isAllowed(String directions, int distance, Pair... points) {
        if (points.length < 5) {
            return false;
        }

        return directions.matches(directionsRegExp);
    }
}
