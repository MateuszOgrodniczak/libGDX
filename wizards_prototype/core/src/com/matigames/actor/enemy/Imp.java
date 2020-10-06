package com.matigames.actor.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.enemy.spell.EnemySpellEnum;
import com.matigames.actor.enemy.spell.SpellAction;

public class Imp extends BaseEnemy {
    private Mage summoner;
    private Animation attackAnimation;
    private SpellAction castPoorMagicbolt;

    public Imp(float x, float y, Mage summoner) {
        super(x, y, summoner.getStage());
        scaleBy(0.4f);

        this.summoner = summoner;

        String idle = "assets/monsters/PNG/jinn_animation/Idle";
        String attack = "assets/monsters/PNG/jinn_animation/Attack";
        String hurt = "assets/monsters/PNG/jinn_animation/Hurt";
        String death = "assets/monsters/PNG/jinn_animation/Death";
        idleAnimation = loadAnimationFromFiles(new String[]{idle + "1.png", idle + "2.png", idle + "3.png"}, 0.25f, true);
        attackAnimation = loadAnimationFromFiles(new String[]{attack + "1.png", attack + "2.png", attack + "4.png"}, 0.25f, false);
        hitAnimation = loadAnimationFromFiles(new String[]{hurt + "1.png", hurt + "2.png"}, 0.25f, false);
        deathAnimation = loadAnimationFromFiles(new String[]{death + "1.png", death + "2.png", death + "3.png", death + "4.png", death + "5.png", death + "6.png"}, 0.15f, false);

        setBoundaryPolygon(8, 5, 3);

        setName("Imp");
        baseHP = 100;
        hp = baseHP;

        castPoorMagicbolt = new SpellAction(this, EnemySpellEnum.POOR_MAGICBOLT);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();

        Animation currentAnimation = getAnimation();
        if (summoner.getStage() == null) {
            setAnimation(deathAnimation);
        }
        if (currentAnimation == deathAnimation) {
            if (isAnimationFinished()) {
                summoner.setSummonedImps(summoner.getSummonedImps() - 1);
                this.remove();
            }
        } else if (currentAnimation == hitAnimation) {
            if (isAnimationFinished()) {
                setAnimation(idleAnimation);
            }
        } else if (currentAnimation == idleAnimation) {
            int result = Mage.randomAction.nextInt(200);
            if (result < 3) {
                setAnimation(attackAnimation);
                addAction(Actions.delay(attackAnimation.getAnimationDuration()));
                addAction(Actions.after(castPoorMagicbolt));
            }
        }
    }

    public Mage getSummoner() {
        return summoner;
    }
}
