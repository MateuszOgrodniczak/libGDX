package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class Icespike extends Spell {

    public static String directionsRegExp = "(R|U)+(R|D)+(L|D)+(R|U)+";

    public Icespike(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/IcePick_64x64.png", 1, 30, 0.1f, true);

        minDmg = 1500;
        maxDmg = 2000;
        manaCost = MANA_COST = 50;
        school = SpellSchool.ICE;
        harmful = true;

        scaleBy(1.5f);

        setSpeed(425);
        setMaxSpeed(425);
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

       /* int proximity = distance/2;
        if (p1.y >= p2.y - distance || p1.x >= p2.x - distance ||
                p3.y >= p2.y - distance || p3.x < p2.x + distance ||
                p4.x < p1.x + distance || p4.y < p1.y + distance ||
                p5.x > p1.x + proximity || p5.x < p1.x - proximity || p5.y > p1.y + proximity || p5.y < p1.y - proximity) {
            return false;
        }*/

        return directions.matches(directionsRegExp);
    }
}
