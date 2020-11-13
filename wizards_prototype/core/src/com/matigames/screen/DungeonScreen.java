package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.matigames.actor.BaseActor;
import com.matigames.actor.Explosion;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.enemy.Orc;
import com.matigames.actor.enemy.OrcWarchief;
import com.matigames.actor.environment.EnvironmentActor;
import com.matigames.actor.environment.Rock;
import com.matigames.actor.spell.Spell;
import com.matigames.actor.spell.SpellSchool;

import java.util.ArrayList;
import java.util.List;

public class DungeonScreen extends BattleScreen {
    private BaseEnemy boss;
    private List<Orc> orcs;
    private boolean bossSpawned;
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

        boss = new OrcWarchief(Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() - 100, null, player);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (!bossSpawned) {
            if (phases >= 3 && counter == 0) {
                bossSpawned = true;
                mainStage.addActor(boss);
                //boss = new OrcWarchief(Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() - 300, mainStage, player);
            } else if (counter % 500 == 0) {
                float xPos = orcsPositions[(int) counter / 500];
                orcs.add(new Orc(xPos, Gdx.graphics.getHeight() - 100, mainStage, player));
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
                    /*for (Orc secondOrc : orcs) {
                        if (orc != secondOrc) {
                            orc.preventOverlap(secondOrc);
                        }
                    }*/
                }
                if (boss != null && boss.overlaps(rock)) {
                    Explosion explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                    explosion.centerAtActor(rock);

                    rock.remove();
                }
            }
        }

        /*for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor instanceof Spell) {
                Spell spell = (Spell) actor;
                if (spell.isHarmful()) {
                    for (int j = 0; j < actors.size; j++) {
                        Actor secondActor = actors.get(j);
                        if (secondActor instanceof BaseEnemy || secondActor instanceof EnvironmentActor) {
                            BaseActor obstacle = (BaseActor) secondActor;
                            if (spell.overlaps(obstacle) && obstacle.getStage() != null) {
                                System.out.println("EXPLOSION!");
                                Explosion explosion;
                                if (spell.getSchool().equals(SpellSchool.FIRE)) {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                                } else if (spell.getSchool().equals(SpellSchool.ICE)) {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 2, 49);
                                } else {
                                    explosion = new Explosion(0, 0, mainStage, (byte) 0, 0);
                                }

                                explosion.centerAtActor(obstacle);
                                spell.remove();
                                if (obstacle instanceof BaseEnemy) {
                                    BaseEnemy baseEnemy = (BaseEnemy) obstacle;
                                    Label damageText = new Label("" + spell.getMinDmg(), skin);
                                    damageText.setPosition(baseEnemy.getX() + 50, baseEnemy.getY());
                                    baseEnemy.setHp(spell.getMinDmg() > baseEnemy.getHp() ? 0 : baseEnemy.getHp() - spell.getMinDmg());

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
        }*/
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
