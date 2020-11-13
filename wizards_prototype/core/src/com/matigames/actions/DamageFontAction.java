package com.matigames.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;


public class DamageFontAction extends TemporalAction {
    private Label damageTextLabel;

    public DamageFontAction(Label damageTextLabel) {
        this.damageTextLabel = damageTextLabel;
    }

    @Override
    protected void update(float percent) {
        damageTextLabel.moveBy(0, 0.5f);
    }
}
