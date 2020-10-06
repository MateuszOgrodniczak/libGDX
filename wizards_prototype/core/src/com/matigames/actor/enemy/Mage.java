package com.matigames.actor.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.enemy.spell.EnemySpellEnum;
import com.matigames.actor.enemy.spell.SpellAction;

import java.util.Random;

public class Mage extends BaseEnemy {
    public static final Random randomAction = new Random();
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> minorAttackAnimation;

    private SpellAction castMissile;
    private SpellAction castMassiveMissile;
    private SpellAction castPoorMagicbolt;
    private SpellAction castFrostbolt;
    private SpellAction castSummonImp;

    private MageType type;
    private int summonedImps;

    public Mage(float x, float y, Stage s) {
        super(x, y, s);

        idleAnimation = loadAnimationFromSheet("assets/monsters/wizard/Idle.png", 1, 6, 0.1f, true);
        deathAnimation = loadAnimationFromSheet("assets/monsters/wizard/Death.png", 1, 7, 0.2f, false);
        hitAnimation = loadAnimationFromSheet("assets/monsters/wizard/Hit.png", 1, 4, 0.2f, false);
        attackAnimation = loadAnimationFromSheet("assets/monsters/wizard/Attack1.png", 1, 8, 0.1f, false);
        minorAttackAnimation = loadAnimationFromSheet("assets/monsters/wizard/Attack2.png", 1, 8, 0.1f, false);

        setBoundaryPolygon(8, 5, 3);

        castPoorMagicbolt = new SpellAction(this, EnemySpellEnum.POOR_MAGICBOLT);

        castMissile = new SpellAction(this, EnemySpellEnum.FIRE_MISSILE);
        castFrostbolt = new SpellAction(this, EnemySpellEnum.FROSTBOLT);

        castMassiveMissile = new SpellAction(this, EnemySpellEnum.MASSIVE_FIRE_MISSILE);

        castSummonImp = new SpellAction(this, EnemySpellEnum.SUMMON_IMP);
        //initMage(type);
    }

    public void initMage(MageType type) {
        this.type = type;
        switch (type) {
            case WILLY:
                initWilly();
                break;
            case APPRENTICE:
                initApprentice();
                break;
            case FLAMETHROWER:
                initFlamethrower();
                break;
            case IMP_MASTER:
                initImpmaster();
                break;
            case PRIEST:
                initPriest();
                break;
            case SNAKE:
                break;
            case FROSTLORD:
                break;
            case BLINKER:
                break;
            case BATTLE_MAGE:
                break;
        }
    }

    private void actMage(MageType type) {
        switch (type) {
            case WILLY:
                actWilly();
                break;
            case APPRENTICE:
                actApprentice();
                break;
            case FLAMETHROWER:
                actFlamethrower();
                break;
            case IMP_MASTER:
                actImpmaster();
                break;
            case PRIEST:
                actPriest();
                break;
            case SNAKE:
                break;
            case FROSTLORD:
                break;
            case BLINKER:
                break;
            case BATTLE_MAGE:
                break;
        }
    }

    private void initWilly() {
        magical = true;
        baseHP = 2000;
        hp = baseHP;
        baseMana = 1000;
        mana = baseMana;
        setName("Little Willy");

        setColor(0.4f, 0.1f, 0.9f, 1);
        scaleBy(0.1f);
    }

    private void initApprentice() {
        magical = true;
        baseHP = 4000;
        hp = baseHP;
        baseMana = 1500;
        mana = baseMana;
        setName("Apprentice");

        scaleBy(0.3f);
    }

    private void initFlamethrower() {
        magical = true;
        baseHP = 5555;
        hp = baseHP;
        baseMana = 1000;
        mana = baseMana;
        setName("Flamethrower");

        setColor(1, 0, 0, 1);
        scaleBy(0.7f);
    }

    private void initImpmaster() {
        magical = true;
        baseHP = 5555;
        hp = baseHP;
        baseMana = 1000;
        mana = baseMana;
        setName("Imp master");

        setColor(0.1f, 0.1f, 0.9f, 1);
        scaleBy(0.5f);
    }

    private void initPriest() {
        idleAnimation = loadAnimationFromSheet("assets/monsters/priest/priest-idle.png", 1, 6, 0.05f, true);
        minorAttackAnimation = loadAnimationFromSheet("assets/monsters/priest/priest-attack.png", 1, 8, 0.05f, false);
        setAnimation(idleAnimation);

        magical = true;
        baseHP = 5555;
        hp = baseHP;
        baseMana = 1000;
        mana = baseMana;
        setName("Priest");

        scaleBy(1.2f);
    }

    private void actWilly() {
        int result = randomAction.nextInt(250);
        if (result < 5) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castPoorMagicbolt));
        } else if (result < 7) {
            setX(getX() + 20);
        } else if (result < 8) {
            setX(getX() - 20);
        }
    }

    private void actApprentice() {
        int result = randomAction.nextInt(250);
        if (result < 5) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castMissile));
        } else if (result < 10) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castFrostbolt));
        }
    }

    private void actImpmaster() {
        int result = randomAction.nextInt(250);
        if (result < 6) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castMissile));
        } else if (result < 8 && summonedImps < 5) {
            summonedImps++;
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castSummonImp));
        }
    }

    private void actFlamethrower() {
        int result = randomAction.nextInt(275);
        if (result < 5) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castMissile));
        } else if (result < 6) {
            setAnimation(attackAnimation);
            addAction(Actions.delay(attackAnimation.getAnimationDuration()));
            addAction(Actions.after(castMassiveMissile));
        }
    }

    private void actPriest() {
        int result = randomAction.nextInt(250);
        if (result < 5) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castMissile));
        } else if (result < 10) {
            setAnimation(minorAttackAnimation);
            addAction(Actions.delay(minorAttackAnimation.getAnimationDuration()));
            addAction(Actions.after(castFrostbolt));
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boundToWorld();

        Animation currentAnimation = getAnimation();
        if (currentAnimation == deathAnimation) {
            if (isAnimationFinished()) {
                this.remove();
            }
        } else if (currentAnimation == hitAnimation) {
            if (isAnimationFinished()) {
                setAnimation(idleAnimation);
            }
        } else if (currentAnimation == idleAnimation) {
            actMage(type);
        }
    }

    public Animation<TextureRegion> getMinorAttackAnimation() {
        return minorAttackAnimation;
    }

    public MageType getType() {
        return type;
    }

    public int getSummonedImps() {
        return summonedImps;
    }

    public void setSummonedImps(int summonedImps) {
        this.summonedImps = summonedImps;
    }
}
