package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

public class FrostNova extends Spell {
    public static String directionsRegExp = "(L|R|U)+(R|D)+(L|R|U)+";
    private static final float w = 200;
    private static final float h = 275;

    public FrostNova(float x, float y, Stage s) {
        super(x-w/2, y-h/2, s);
        minDmg = 800;
        maxDmg = 1200;
        school = SpellSchool.ICE;
        setHarmful(true);
        setFreezing(true);
    }

    public void init(Stage s) {
        setStage(s);

        String[] animations = new String[12];
        for(int i = 1; i<13; i++) {
            String frameNumber = i <= 9 ? ("0" + i) : ""+i;
            animations[i-1] = "assets/ice/strong/strong_ice00" + frameNumber + ".png";
        }
        loadAnimationFromFiles(animations, 0.1f, false);
        float disappearAfter = 0.1f * animations.length - 0.5f;

       /* setX(x);
        setY(y);
        setWidth(175);
        setHeight(250);*/

        setWidth(w);
        setHeight(h);
        setBoundaryPolygon(8, 2, 1);

        setSpeed(0);

        // scaleBy(1.5f);
        addAction(Actions.delay(disappearAfter));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(getStage() != null) {
            applyPhysics(delta);
            boundToWorld();
        }
    }

    public static boolean isAllowed(String directions, int distance, Pair... points) {
        if (points.length < 5) {
            return false;
        }

        Pair p1 = points[0];
        Pair p2 = points[1];
        Pair p3 = points[2];
        Pair p4 = points[3];

        /*if(p1.x < p2.x-5 || p1.x > p2.x+5 ||
                p3.x < p4.x-5 || p3.x > p4.x+5 ||
                p1.y < p3.y-5 || p1.y > p3.y+5 ||
                p2.y < p4.y-5 || p2.y > p4.y+5 ||
                p1.y >= p2.y-20 ||
                p3.y >= p4.y-20 ||
                p2.x >= p3.x-20 || p2.x >= p4.x-20) {
            return false;
        }*/
        return directions.matches(directionsRegExp);
    }
}
