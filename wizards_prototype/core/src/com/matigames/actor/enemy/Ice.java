package com.matigames.actor.enemy;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;

public class Ice extends BaseActor {

    public Ice(float x, float y, Stage s, BaseEnemy enemy) {
        super(x, y, s);

        /*String[] animations = new String[4];
        for(int i = 9; i<13; i++) {
            String frameNumber = i <= 9 ? ("0" + i) : ""+i;
            animations[i-9] = "assets/ice/strong/strong_ice00" + frameNumber + ".png";
        }*/
        String fileName = "assets/ice/strong/strong_ice0011";
        loadAnimationFromSheet(fileName,1, 1, 1, true);

        setWidth(enemy.getWidth());
        setHeight(enemy.getHeight()/2);

        setX(enemy.getX());
        setY(enemy.getY());
    }
}
