package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;
import com.matigames.actor.Explosion;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public abstract class Spell extends BaseActor {
    protected SpellSchool school;
    protected float manaCost;
    protected float baseDmg;
    protected boolean harmful;

    public static float MANA_COST;

    public Spell(float x, float y, Stage s) {
        super(x, y, s);
    }

    public float getBaseDmg() {
        return baseDmg;
    }

    public SpellSchool getSchool() {
        return school;
    }

    public float getManaCost() {
        return manaCost;
    }

    public boolean isHarmful() {
        return harmful;
    }

    public void setHarmful(boolean harmful) {
        this.harmful = harmful;
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        // System.out.println("Spell coordinates: x: " + x + ", y: " + y);
        if (getSchool() != null && (x >= GAME_WIDTH - width || x <= 0 || y >= GAME_HEIGHT - height || y <= 0)) {
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
}
