package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class Tornado extends Spell {
    public static String directionsRegExp = "(L|U)+(U|R)+(R|D)+(D|L)+(L|U)+(U|R)+";

    public Tornado(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/TornadoMoving_96x96.png", 1, 89, 0.025f, true);
        setBoundaryPolygon(3, 1, 1);

        school = SpellSchool.STORM;
        minDmg = 1000;
        manaCost = 10;
        harmful = true;

        scaleBy(1.75f);

        setSpeed(100);
        setMaxSpeed(100);
        setDeceleration(0);

        // setRotation(90);
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

        Pair p1 = points[0];
        Pair p2 = points[1];
        Pair p3 = points[2];
        Pair p4 = points[3];
        Pair p5 = points[4];
        if (p1.y >= p5.y || p1.x + distance < p5.x || p1.x - distance > p5.x) {
            return false;
        }

        return true;//directions.matches(directionsRegExp);
    }

    public float getMinDmg() {
        return this.minDmg;
    }
}
