package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class Fireball extends Spell {

    public static String directionsRegExp = "(L|U)+(U|R)+(R|D)+(D|L)+(L|U)+(U|R)+";

    public Fireball(float x, float y, Stage s, byte level, int frames) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/fireball_" + level + "_64x64.png", 1, frames, 0.025f, true);
        setBoundaryPolygon(3, 1, 1);

        school = SpellSchool.FIRE;
        minDmg = 1000 * level;
        maxDmg = minDmg + 750;
        MANA_COST = 25;
        manaCost = 25 * level;
        harmful = true;

        scaleBy(level * 1.5f);

        setSpeed(400);
        setMaxSpeed(400);
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

        Pair p1 = points[0];
        Pair p2 = points[1];
        Pair p3 = points[2];
        Pair p4 = points[3];
        Pair p5 = points[4];
        if (p1.y >= p2.y - distance || p4.y >= p1.y - distance || p5.x >= p1.x - distance || p1.x >= p3.x - distance) {
            return false;
        }

        return directions.matches(directionsRegExp);
    }

    public float getMinDmg() {
        return this.minDmg;
    }
}
