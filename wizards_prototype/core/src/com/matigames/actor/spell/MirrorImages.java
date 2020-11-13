package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.Player;
import com.matigames.util.Pair;

import java.util.List;

public class MirrorImages extends Spell{
    public static String directionsRegExp = "(L|U)+(U|R)+(R|D)+(D|L)+(L|U)+(U|R)+";
    private final int xDisplacement = 100;

    public MirrorImages(Stage s, Player caster) {
        super(0, 0, s);

        drawable = false;
        harmful = false;

        List<Mirror> casterMirrors = caster.getMirrors();
        for(Mirror mirror : casterMirrors) {
            mirror.remove();
        }
        casterMirrors.clear();

        casterMirrors.add(new Mirror(s, caster, xDisplacement));
        casterMirrors.add(new Mirror(s, caster, -xDisplacement));
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
}
