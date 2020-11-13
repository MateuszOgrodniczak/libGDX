package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.util.GenericUtil;

public class FallingIce extends Spell {
    public FallingIce(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/test-spells/IcePick_64x64.png", 1, 30, 0.1f, true);
        setBoundaryPolygon(3, 1, 1);

        school = SpellSchool.ICE;
        harmful = true;
        minDmg = 600;
        maxDmg = 1000;
        penetrable = true;
        calculateSpellDmg(this);

        scaleBy(2f);
        setBoundaryPolygon(3, 1, 1);
        //setColor(1, 0.8f, 0.8f, 1);

        setSpeed(200);
        setMaxSpeed(200);
        setDeceleration(0);

        setRotation(-60);
        setMotionAngle(-60);

        addAction(Actions.delay(2));
        addAction(Actions.after(Actions.removeActor()));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();
    }

    private void calculateSpellDmg(Spell spell) {
        if (spell.isHarmful()) {
            float dmg = GenericUtil.random.nextInt((int) spell.getMaxDmg()) + spell.getMinDmg();
            boolean isCrit = GenericUtil.random.nextInt(3) > 1;
            if (isCrit) {
                spell.setCrit(true);
                dmg *= 2;
            }
            spell.setDmg(dmg);
        }
    }
}
