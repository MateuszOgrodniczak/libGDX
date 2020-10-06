package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.matigames.actor.Player;

public class MagicBarrier extends Spell {
    private Player player;
    private float power;

    public MagicBarrier(float x, float y, Stage s, Player player) {
        super(x, y, s);

        this.player = player;
        this.power = 500;
        school = SpellSchool.ARCANE;

        loadTexture("assets/test-spells/shields.png");

        float width = player.getWidth() / getWidth() + 0.15f;
        float height = player.getHeight() / getHeight() + 0.35f;
        Action pulse = Actions.sequence(
                Actions.scaleTo(width, height, 1), Actions.scaleTo(width - 0.2f, height - 0.2f, 1));

        addAction(Actions.forever(pulse));
    }

    @Override
    public void act(float dt) {
        super.act(dt);
        this.setOpacity(power / 500f);
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }
}
