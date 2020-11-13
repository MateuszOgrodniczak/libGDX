package com.matigames.actor.spell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;
import com.matigames.actor.Direction;
import com.matigames.actor.Player;

public class Mirror extends BaseActor {
    private Player caster;
    private int xDisplacement;

    //animations
    Animation<TextureRegion> movingAnimation;
    Animation<TextureRegion> deathAnimation;
    Animation<TextureRegion> idleAnimation;

    public Mirror(Stage s, Player caster, int xDisplacement) {
        super(caster.getX()+xDisplacement, caster.getY(), s);
        this.caster = caster;
        this.xDisplacement = xDisplacement;

        idleAnimation = loadAnimationFromSheet("assets/player/Idle.png", 1, 8, 0.05f, true);
        movingAnimation = loadAnimationFromSheet("assets/player/Move.png", 1, 8, 0.1f, true);
        deathAnimation = loadAnimationFromSheet("assets/player/Death.png", 1, 5, 0.01f, false);

        setBoundaryPolygon(8, 5, 3);
        scaleBy(2f);
        changeCurrentAnimation();

    }

    @Override
    public void act(float delta) {
        super.act(delta);

        setX(caster.getX() + xDisplacement);
        setY(caster.getY());

        changeCurrentAnimation();
        direction = caster.getDirection();

        applyPhysics(delta);
        boundToWorld();
    }

    private void changeCurrentAnimation() {
        Animation mirrorAnimation = getAnimation();
        Animation animation = caster.getAnimation();
        if(mirrorAnimation != idleAnimation && animation == caster.getIdleAnimation()) {
            setAnimation(idleAnimation);
        } else if(mirrorAnimation != movingAnimation && animation == caster.getMovingAnimation()) {
            setAnimation(movingAnimation);
        } else if(mirrorAnimation != deathAnimation && animation == caster.getDeathAnimation()) {
            setAnimation(deathAnimation);
        }
    }
}
