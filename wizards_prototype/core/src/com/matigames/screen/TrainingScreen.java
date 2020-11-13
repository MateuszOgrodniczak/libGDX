package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.enemy.Dummy;

import java.util.ArrayList;
import java.util.List;

public class TrainingScreen extends BattleScreen {
    //test
    protected Dummy dummy;
    private List<BaseEnemy> enemies;

    public TrainingScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        enemies = new ArrayList<>();
        dummy = new Dummy(Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() - 250, mainStage);
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

        /*Array<Actor> actors = mainStage.getActors();

        for (Actor actor : actors) {
            if (actor instanceof Spell) {
                Spell spell = (Spell) actor;
                if (spell.isHarmful()) {
                    for (Actor secondActor : actors) {
                        if (secondActor instanceof BaseEnemy || secondActor instanceof EnvironmentActor) {
                            BaseActor obstacle = (BaseActor) secondActor;
                            if (obstacle instanceof Mage && ((Mage) obstacle).getType() == MageType.FLAMETHROWER) {
                                int result = Mage.randomAction.nextInt(4);
                                if (result < 2) {
                                    System.out.println("REFLECTED: " + result);
                                    int angleChange;
                                    if (spell.getX() >= obstacle.getX() + obstacle.getWidth() / 2) {
                                        angleChange = -45;
                                    } else {
                                        angleChange = 45;
                                    }
                                    spell.setRotation(spell.getRotation() + angleChange);
                                    spell.setMotionAngle(spell.getMotionAngle() + angleChange);
                                    spell.setHarmful(false);
                                    //return;
                                }
                            }
                            if (spell.isHarmful() && spell.overlaps(obstacle) && obstacle.getStage() != null) {
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
                                        baseEnemy.setAnimation(baseEnemy.getDeathAnimation());
                                    } else {
                                        baseEnemy.setAnimation(baseEnemy.getHitAnimation());
                                    }
                                }
                            }
                        }
                    }
                }


                /*if (spell.isHarmful() && spell.overlaps(dummy)) {
                    System.out.println("EXPLOSION!");
                    Explosion explosion;
                    if (spell.getSchool().equals(SpellSchool.FIRE)) {
                        explosion = new Explosion(0, 0, mainStage, (byte) 1, 44);
                    } else if (spell.getSchool().equals(SpellSchool.ICE)) {
                        explosion = new Explosion(0, 0, mainStage, (byte) 2, 49);
                        dummy.setSlowed(true);
                    } else {
                        explosion = new Explosion(0, 0, mainStage, (byte) 0, 0);
                    }

                    explosion.centerAtActor(dummy);

                    spell.remove();


                    // damageTextStyle.font.getData().scale(0.25f);
                    Actor target = dummy;
                    Direction direction;
                    if ((target.getX() + target.getWidth() / 2) < Gdx.graphics.getWidth() / 2f) {
                        direction = Direction.LEFT;
                    } else {
                        direction = Direction.RIGHT;
                    }
                    DamageText damageText = new DamageText("" + spell.getDmg(), skin, direction, spell.isCrit());
                    damageText.setPosition(dummy.getX() + 50, dummy.getY() + dummy.getHeight() / 2);
                    // damageTextStyle.font.getData().scale(-0.25f);

                    mainStage.addActor(damageText);
                }
            }*/

    }
}
