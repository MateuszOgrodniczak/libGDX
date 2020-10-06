package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.matigames.actor.Explosion;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.enemy.Mage;
import com.matigames.actor.enemy.MageType;
import com.matigames.actor.spell.Spell;
import com.matigames.actor.spell.SpellSchool;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class TowerScreen extends BattleScreen {
    //test
    private MageType type;
    protected Mage mage;

    public TowerScreen(Game game, MageType type) {
        super(game);
        this.type = type;
        mage.initMage(type);
    }

    @Override
    public void initialize() {
        super.initialize();

        mage = new Mage(GAME_WIDTH / 2 - 100, GAME_HEIGHT - 250, mainStage);
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
        Array<Actor> actors = mainStage.getActors();

        for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor instanceof Spell) {
                Spell spell = (Spell) actor;
                if (spell.isHarmful()) {
                    for (int j = 0; j < actors.size; j++) {
                        Actor enemy = actors.get(j);
                        if (enemy instanceof BaseEnemy) {
                            BaseEnemy baseEnemy = (BaseEnemy) enemy;
                            if (spell.overlaps(baseEnemy) && baseEnemy.getStage() != null) {
                                //mage
                                if (baseEnemy instanceof Mage && type == MageType.FLAMETHROWER) {
                                    int result = Mage.randomAction.nextInt(4);
                                    if (result < 2) {
                                        System.out.println("REFLECTED: " + result);
                                        int angleChange;
                                        if (spell.getX() >= mage.getX() + mage.getWidth() / 2) {
                                            angleChange = -45;
                                        } else {
                                            angleChange = 45;
                                        }
                                        spell.setRotation(spell.getRotation() + angleChange);
                                        spell.setMotionAngle(spell.getMotionAngle() + angleChange);
                                        spell.setHarmful(false);
                                        return;
                                    }
                                }

                                System.out.println("EXPLOSION!");
                                Explosion explosion;
                                if (spell.getSchool().equals(SpellSchool.FIRE)) {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                                } else if (spell.getSchool().equals(SpellSchool.ICE)) {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 2, 49);
                                } else {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 0, 0);
                                }

                                explosion.centerAtActor(baseEnemy);

                                spell.remove();

                                Label damageText = new Label("" + spell.getBaseDmg(), skin);
                                damageText.setPosition(baseEnemy.getX() + 50, baseEnemy.getY());
                                baseEnemy.setHp(spell.getBaseDmg() > baseEnemy.getHp() ? 0 : baseEnemy.getHp() - spell.getBaseDmg());

                                damageText.addAction(Actions.delay(1));
                                damageText.addAction(Actions.after(Actions.fadeOut(0.5f)));
                                damageText.addAction(Actions.after(Actions.removeActor()));
                                mainStage.addActor(damageText);

                                if (baseEnemy.getHp() <= 0) {
                                    baseEnemy.getNameLabel().remove();
                                    baseEnemy.setAnimation(baseEnemy.getDeathAnimation());
                                } else {
                                    baseEnemy.setAnimation(baseEnemy.getHitAnimation());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
