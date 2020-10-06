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
import com.matigames.actor.enemy.Orc;
import com.matigames.actor.enemy.OrcWarchief;
import com.matigames.actor.environment.Rock;
import com.matigames.actor.spell.Spell;
import com.matigames.actor.spell.SpellSchool;

import java.util.ArrayList;
import java.util.List;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class DungeonScreen extends BattleScreen {
    private BaseEnemy boss;
    private List<Orc> orcs;
    private int[] orcsPositions;

    private long counter;
    private int phases;

    public DungeonScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        orcsPositions = new int[]{100, 500, 800};
        orcs = new ArrayList<>();

        new Rock(300, 250, mainStage);
        new Rock(200, 400, mainStage);
        new Rock(400, 400, mainStage);
        new Rock(600, 350, mainStage);
        new Rock(750, 500, mainStage);
        new Rock(600, 650, mainStage);
        new Rock(300, 600, mainStage);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (boss == null) {
            if (phases >= 3 && counter == 0) {
                boss = new OrcWarchief(GAME_WIDTH / 2 - 100, GAME_HEIGHT - 300, mainStage, player);
            } else if (counter % 500 == 0) {
                float xPos = orcsPositions[(int) counter / 500];
                orcs.add(new Orc(xPos, GAME_HEIGHT - 100, mainStage, player));
                if (counter == 1000) {
                    counter = -495;
                    phases++;
                }
            }

            counter += 5;
        }

        Array<Actor> actors = mainStage.getActors();
        for (Actor actor : actors) {
            if (actor instanceof Rock) {
                Rock rock = (Rock) actor;
                player.preventOverlap(rock);
                for (Orc orc : orcs) {
                    orc.preventOverlap(rock);
                    for (Orc secondOrc : orcs) {
                        if (orc != secondOrc) {
                            orc.preventOverlap(secondOrc);
                        }
                    }
                }
                if (boss != null && boss.overlaps(rock)) {
                    Explosion explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                    explosion.centerAtActor(rock);

                    rock.remove();
                }
            }
        }

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
                                    baseEnemy.remove();
                                    //  baseEnemy.setAnimation(baseEnemy.getDeathAnimation());
                                } else {
                                    //baseEnemy.setAnimation(baseEnemy.getHitAnimation());
                                }
                            }
                        }
                    }
                }
            }
        }
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
}
