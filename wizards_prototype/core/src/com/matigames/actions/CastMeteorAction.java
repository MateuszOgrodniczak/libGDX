package com.matigames.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.spell.Meteor;
import com.matigames.util.Pair;

import static com.matigames.util.GenericUtil.random;

public class CastMeteorAction extends Action {
    private Pair positions;
    private Stage stage;

    public CastMeteorAction(Pair meteorPositions, Stage stage) {
        this.positions = meteorPositions;
        this.stage = stage;
    }

    @Override
    public boolean act(float delta) {
        int x = random.nextInt(Gdx.graphics.getWidth() - 50) + 25;
        int y = random.nextInt(Gdx.graphics.getHeight() - 50) + 25;
        new Meteor(x, y, stage);
        return true;
    }
}
