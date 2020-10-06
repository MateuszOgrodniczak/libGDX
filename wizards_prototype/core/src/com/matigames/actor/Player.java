package com.matigames.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.spell.MagicBarrier;
import com.matigames.actor.spell.Spell;

public class Player extends BaseActor {
    private Spell shield;

    private final float baseHP = 300;
    private final float baseMana = 1000;
    private float hp;
    private float mana;

    //animations
    Animation<TextureRegion> movingAnimation;
    Animation<TextureRegion> deathAnimation;
    Animation<TextureRegion> idleAnimation;

    public float hpLeft() {
        return (hp / baseHP);
    }

    public float manaLeft() {
        return (mana / baseMana);
    }

    public void addHp(float hp) {
        this.hp += hp;
    }

    public void addMana(float mana) {
        this.mana += mana;
    }

    public Player(float x, float y, Stage s) {
        super(x, y, s);

        idleAnimation = loadAnimationFromSheet("assets/player/Idle.png", 1, 8, 0.05f, true);
        movingAnimation = loadAnimationFromSheet("assets/player/Move.png", 1, 8, 0.1f, true);
        deathAnimation = loadAnimationFromSheet("assets/player/Death.png", 1, 5, 0.01f, false);

        setBoundaryPolygon(8, 5, 3);
        //setBoundaryRectangle();
        scaleBy(2);

        setOriginX(getWidth() / 2);
        setOriginY(getHeight() / 2);

        direction = Direction.RIGHT;
        setSpeed(15);
        setAcceleration(0);

        hp = baseHP;
        mana = baseMana;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Animation currentAnimation = getAnimation();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (currentAnimation != movingAnimation) {
                setAnimation(movingAnimation);
            }
            direction = Direction.LEFT;
            moveBy(-5, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (currentAnimation != movingAnimation) {
                setAnimation(movingAnimation);
            }
            direction = Direction.RIGHT;
            moveBy(5, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            setAnimation(deathAnimation);
        } else if (currentAnimation != idleAnimation) {
            setAnimation(idleAnimation);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (shield != null) {
                shield.remove();
            }
            shield = new MagicBarrier(0, 0, getStage(), this);
            addActor(shield);
            //shield.centerAtActor(this);
            shield.setPosition(getWidth() / 2 - shield.getWidth() / 2, getHeight() / 2 - shield.getHeight() / 2);
        }

        applyPhysics(delta);
        boundToWorld();
    }

    public Spell getShield() {
        return shield;
    }

    public void setShield(Spell shield) {
        this.shield = shield;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }
}
