package com.matigames.actor.spell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;
import com.matigames.actor.Explosion;
import com.matigames.actor.Player;
import com.matigames.util.GenericUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Spell extends BaseActor {
    protected SpellSchool school;
    protected float manaCost;

    protected float minDmg;
    protected float maxDmg;
    protected float dmg;
    protected boolean harmful;
    protected boolean freezing;
    protected boolean crit;
    protected boolean penetrable;
    protected List<Spell> subspells = new ArrayList<>();

    public static float MANA_COST;

    public Spell(float x, float y, Stage s) {
        super(x, y, s);
    }

    public float getMinDmg() {
        return minDmg;
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
        if(this.getStage() == null) {
            return;
        }

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
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

    public boolean isCrit() {
        return crit;
    }

    public void setCrit(boolean crit) {
        this.crit = crit;
    }

    public float getDmg() {
        return dmg;
    }

    public void setDmg(float dmg) {
        this.dmg = dmg;
    }

    public void setMinDmg(float minDmg) {
        this.minDmg = minDmg;
    }

    public float getMaxDmg() {
        return maxDmg;
    }

    public void setMaxDmg(float maxDmg) {
        this.maxDmg = maxDmg;
    }

    public boolean isFreezing() {
        return freezing;
    }

    public void setFreezing(boolean freezing) {
        this.freezing = freezing;
    }

    public List<Spell> getSubspells() {
        return subspells;
    }

    public boolean isPenetrable() {
        return penetrable;
    }
}
