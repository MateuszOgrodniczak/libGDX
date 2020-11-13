package com.matigames.actor.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.Direction;
import com.matigames.actor.Player;

public class Orc extends BaseEnemy {
    private Player target;

    //animations
    static Animation<TextureRegion> movingAnimation;
    static Animation<TextureRegion> deathAnimation;
    static Animation<TextureRegion> idleAnimation;
    static Animation<TextureRegion> attackAnimation;

    public Orc(float x, float y, Stage s, Player target) {
        super(x, y, s);
        this.target = target;

        String[] idle = new String[]{"assets/monsters/orcs/1_ORK/IDLE/IDLE_000.png", "assets/monsters/orcs/1_ORK/IDLE/IDLE_001.png",
                "assets/monsters/orcs/1_ORK/IDLE/IDLE_002.png", "assets/monsters/orcs/1_ORK/IDLE/IDLE_003.png",
                "assets/monsters/orcs/1_ORK/IDLE/IDLE_004.png", "assets/monsters/orcs/1_ORK/IDLE/IDLE_005.png",
                "assets/monsters/orcs/1_ORK/IDLE/IDLE_006.png"};

        String[] run = new String[]{"assets/monsters/orcs/1_ORK/RUN/RUN_000.png", "assets/monsters/orcs/1_ORK/RUN/RUN_001.png",
                "assets/monsters/orcs/1_ORK/RUN/RUN_002.png", "assets/monsters/orcs/1_ORK/RUN/RUN_003.png",
                "assets/monsters/orcs/1_ORK/RUN/RUN_004.png", "assets/monsters/orcs/1_ORK/RUN/RUN_005.png",
                "assets/monsters/orcs/1_ORK/RUN/RUN_006.png"};

        String[] attack = new String[]{"assets/monsters/orcs/1_ORK/ATTAK/ATTAK_000.png", "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_001.png",
                "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_002.png", "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_003.png",
                "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_004.png", "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_005.png",
                "assets/monsters/orcs/1_ORK/ATTAK/ATTAK_006.png"};

        String[] death = new String[]{"assets/monsters/orcs/1_ORK/DIE/DIE_000.png", "assets/monsters/orcs/1_ORK/DIE/DIE_001.png",
                "assets/monsters/orcs/1_ORK/DIE/DIE_002.png", "assets/monsters/orcs/1_ORK/DIE/DIE_003.png",
                "assets/monsters/orcs/1_ORK/DIE/DIE_004.png", "assets/monsters/orcs/1_ORK/DIE/DIE_005.png",
                "assets/monsters/orcs/1_ORK/DIE/DIE_006.png"};

        if (movingAnimation == null) {
            movingAnimation = loadAnimationFromFiles(run, 0.1f, true);
            attackAnimation = loadAnimationFromFiles(attack, 0.1f, true);
            deathAnimation = loadAnimationFromFiles(death, 0.1f, false);
        }

        setAnimation(movingAnimation);
        setSize(75, 100);
        setBoundaryPolygon(8, 1, 1);

        baseHP = 5000;
        hp = baseHP;

        baseSpeed = 125;
        setSpeed(125);
        setMaxSpeed(125);
        setDeceleration(0);
    }

    @Override
    public void setAnimation(Animation<TextureRegion> animation) {
        super.setAnimation(animation);
        setSize(75, 100);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();

        Animation<TextureRegion> currentAnimation = getAnimation();

        if (currentAnimation == deathAnimation) {
            if (isAnimationFinished()) {
                this.remove();
            }
        } else if (inRange()) {
            //attack
            if (currentAnimation != attackAnimation) {
                setAnimation(attackAnimation);
            }
        } else {
            if (currentAnimation != movingAnimation) {
                setAnimation(movingAnimation);
            }

            float xChange = clap(target.getX(), getX());
            if (xChange >= 0) {
                direction = Direction.RIGHT;
            } else {
                direction = Direction.LEFT;
            }
            double angle = Math.atan2(target.getY() - getY(), target.getX() - getX());
            angle = Math.toDegrees(angle);
            setMotionAngle((float) angle);
        }
    }

    private float clap(float pos1, float pos2) {
        float result = pos1 - pos2;
        if (result <= -1.5) {
            return -1.5f;
        }
        if (result >= 1.5f) {
            return 1.5f;
        }
        return result;
    }

    private boolean inRange() {
        return Math.abs(target.getX() - getX()) <= 20 && Math.abs(target.getY() - getY()) <= 20;
    }

    public Animation getDeathAnimation() {
        return deathAnimation;
    }
    public Animation getMovingAnimation() {
        return movingAnimation;
    }
    public Animation getAttackAnimation() {
        return attackAnimation;
    }
}
