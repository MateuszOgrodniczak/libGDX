package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class Blink extends Spell {

    public static String directionsRegExp = "(U|R)+";

    public Blink(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/warp.png", 4, 8, 0.05f, true);

        manaCost = MANA_COST = 50;
        scaleBy(1.5f);
        harmful = false;
        school = SpellSchool.ARCANE;

        addAction(Actions.delay(1));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
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
        if (p1.x >= p5.x || p1.y >= p5.y) {
            return false;
        }

        return directions.matches(directionsRegExp);
    }
}
