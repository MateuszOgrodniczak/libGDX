package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class SummonImp extends EnemySpell {
    public SummonImp(float x, float y, Stage s, int summonedImps, float summonerX, float summonerWidth) {
        super(x, y, s);

        if (summonedImps % 2 != 0) {
            setX(summonerX + summonerWidth + summonedImps * 15);
        } else {
            setX(summonerX - summonedImps * 15);
        }
    }
}
