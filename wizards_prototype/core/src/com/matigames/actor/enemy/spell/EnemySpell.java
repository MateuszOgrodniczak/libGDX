package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;

import java.util.Random;

public abstract class EnemySpell extends BaseActor {
    static final Random randomAngle = new Random();

    public EnemySpell(float x, float y, Stage s) {
        super(x, y, s);
    }
}
