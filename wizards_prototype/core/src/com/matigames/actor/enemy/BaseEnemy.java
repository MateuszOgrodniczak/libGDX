package com.matigames.actor.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;

import java.util.Random;

public abstract class BaseEnemy extends BaseActor {
    protected static final Random randomAction = new Random();
    protected Animation<TextureRegion> movingAnimation;
    protected Animation<TextureRegion> deathAnimation;
    protected Animation<TextureRegion> hitAnimation;
    protected Animation<TextureRegion> idleAnimation;

    protected boolean magical;
    protected float baseHP;
    protected float baseMana;
    protected float hp;
    protected float mana;

    protected boolean immortal;
    protected boolean slowed;
    protected boolean frozen;
    protected boolean stunned;
    protected boolean shocked;

    protected float baseSpeed;

    public BaseEnemy(float x, float y, Stage s) {
        super(x, y, s);
    }

    public boolean isMagical() {
        return magical;
    }

    public float getBaseHP() {
        return baseHP;
    }

    public float getBaseMana() {
        return baseMana;
    }

    public float getHp() {
        return hp;
    }

    public float getMana() {
        return mana;
    }

    public boolean isImmortal() {
        return immortal;
    }

    public float hpLeft() {
        return (hp / baseHP);
    }

    public float manaLeft() {
        return (mana / baseMana);
    }

    public void setMagical(boolean magical) {
        this.magical = magical;
    }

    public void setBaseHP(float baseHP) {
        this.baseHP = baseHP;
    }

    public void setBaseMana(float baseMana) {
        this.baseMana = baseMana;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }

    public void setImmortal(boolean immortal) {
        this.immortal = immortal;
    }

    public Animation<TextureRegion> getMovingAnimation() {
        return movingAnimation;
    }

    public Animation<TextureRegion> getDeathAnimation() {
        return deathAnimation;
    }

    public Animation<TextureRegion> getHitAnimation() {
        return hitAnimation;
    }

    public Animation<TextureRegion> getIdleAnimation() {
        return idleAnimation;
    }

    public boolean isSlowed() {
        return slowed;
    }

    public void setSlowed(boolean slowed) {
        this.slowed = slowed;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isStunned() {
        return stunned;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public boolean isShocked() {
        return shocked;
    }

    public void setShocked(boolean shocked) {
        this.shocked = shocked;
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        if(slowed) {
            setColor(Color.SKY);
            setSpeed(getSpeed()*0.5f);
        }
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }
}
