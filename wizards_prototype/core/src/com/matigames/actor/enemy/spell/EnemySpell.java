package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;
import com.matigames.actor.Explosion;
import com.matigames.actor.spell.SpellSchool;

import java.util.Random;

public abstract class EnemySpell extends BaseActor {
    static final Random randomAngle = new Random();
    protected SpellSchool school;

    public EnemySpell(float x, float y, Stage s) {
        super(x, y, s);
    }

    @Override
    public void act(float dt) {
        super.act(dt);
        if(this.getStage() == null) {
            return;
        }

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        // System.out.println("Spell coordinates: x: " + x + ", y: " + y);
        if (getSchool() != null && (x >= Gdx.graphics.getWidth() - width || x <= 0 || y >= Gdx.graphics.getHeight() - height || y <= 0)) {
            Explosion explosion = null;
            if (this.getSchool().equals(SpellSchool.FIRE)) {
                explosion = new Explosion(x, y, this.getStage(), (byte) 1, 44);
            } else if (this.getSchool().equals(SpellSchool.ICE)) {
                explosion = new Explosion(x, y, this.getStage(), (byte) 2, 49);
            } else {
                //explosion = new Explosion(x, y, this.getStage(), (byte) 0, 0);
            }

            if (explosion != null) {
                this.getStage().addActor(explosion);
            }

            this.remove();
        }
    }

    public SpellSchool getSchool() {
        return school;
    }

    public void setSchool(SpellSchool school) {
        this.school = school;
    }
}
