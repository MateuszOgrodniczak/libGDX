package com.matigames.actor.spell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.Pair;

import java.util.List;

import static com.matigames.util.GenericUtil.random;

public class Blizzard extends Spell {
    private static final int METEORS_TOTAL = 25;
    public static String directionsRegExp = "(R|U)+(R|D)+(L|D)+(R|U)+";

    public Blizzard(float x, float y, Stage s) {
        super(x, y, s);

        drawable = false;
        harmful = false;

        for (int i = 0; i < METEORS_TOTAL; i++) {
            addAction(Actions.after(Actions.delay(0.5f)));
            addAction(Actions.after(new CastBlizzardAction(this.getStage(), subspells)));
        }
    }

    public class CastBlizzardAction extends Action {
        private Stage stage;
        private List<Spell> meteors;

        CastBlizzardAction(Stage stage, List<Spell> meteors) {
            this.stage = stage;
            this.meteors = meteors;
        }

        @Override
        public boolean act(float delta) {
            float x = random.nextInt(Gdx.graphics.getWidth()/3) + 50f;
            float y = random.nextInt(Gdx.graphics.getHeight()/4 - 100) + Gdx.graphics.getHeight()*0.75f;
            meteors.add(new FallingIce(x, y, stage));
            return true;
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
