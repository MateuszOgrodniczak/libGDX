package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.matigames.actor.Explosion;
import com.matigames.actor.enemy.Dummy;
import com.matigames.actor.spell.Spell;
import com.matigames.actor.spell.SpellSchool;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class TrainingScreen extends BattleScreen {
    //test
    protected Dummy dummy;

    public TrainingScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        dummy = new Dummy(GAME_WIDTH / 2 - 100, GAME_HEIGHT - 250, mainStage);
    }

    @Override
    public void show() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(this);
        im.addProcessor(uiStage);
        im.addProcessor(mainStage);
    }

    @Override
    public void hide() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        im.removeProcessor(uiStage);
        im.removeProcessor(mainStage);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Array<Actor> actors = mainStage.getActors();

        for (Actor actor : actors) {
            if (actor instanceof Spell) {
                Spell spell = (Spell) actor;
                if (spell.overlaps(dummy)) {
                    System.out.println("EXPLOSION!");
                    Explosion explosion;
                    if (spell.getSchool().equals(SpellSchool.FIRE)) {
                        explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                    } else if (spell.getSchool().equals(SpellSchool.ICE)) {
                        explosion = new Explosion(0, 0, mainStage, (byte) 2, 49);
                    } else {
                        explosion = new Explosion(0, 0, mainStage, (byte) 0, 0);
                    }

                    explosion.centerAtActor(dummy);

                    spell.remove();

                    Label damageText = new Label("" + spell.getBaseDmg(), skin);
                    damageText.setPosition(dummy.getX() + 50, dummy.getY());
                    damageText.scaleBy(2);

                    damageText.addAction(Actions.delay(1));
                    damageText.addAction(Actions.after(Actions.fadeOut(0.5f)));
                    damageText.addAction(Actions.after(Actions.removeActor()));
                    mainStage.addActor(damageText);
                }
            }

        }

    }
}
