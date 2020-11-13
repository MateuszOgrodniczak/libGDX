package com.matigames.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.matigames.actor.*;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.enemy.Mage;
import com.matigames.actor.enemy.MageType;
import com.matigames.actor.environment.EnvironmentActor;
import com.matigames.actor.spell.*;
import com.matigames.config.GlobalConfig;
import com.matigames.util.GenericUtil;
import com.matigames.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.matigames.config.GlobalConfig.touchpadOn;

public abstract class BattleScreen extends BaseScreen {

    private static final float BUTTON_WIDTH = Gdx.graphics.getWidth() * 0.15f;
    private static final float BUTTON_HEIGHT = Gdx.graphics.getHeight() * 0.05f;

    //android
    private Touchpad touchpad;

    private Pair previousPosition;
    protected ShapeRenderer shapeRenderer;
    protected Player player;
    protected List<SpellEnum> availableSpells;
    protected int xPosition;
    protected int yPosition;

    //colors
    protected Color runeColor;
    protected Color cursorColor;
    protected Color blue;
    protected Color red;
    protected Color white;
    protected Color green;
    protected Color yellow;

    protected List<Pair> runePositions;
    protected StringBuilder runeRegex;
    protected boolean spellCasted;
    protected Spell currentSpell;
    private List<Spell> currentSpellCopies;
    private Spell[] spellBuildup;

    //flags
    private static final byte SPELL_LEVEL_1 = 1;
    private static final byte SPELL_LEVEL_2 = 2;
    private static final byte SPELL_LEVEL_3 = 3;

    //text
    protected TextField.TextFieldStyle damageTextStyle;

    public BattleScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (touchpadOn) {
            drawTouchpad();
        }

        damageTextStyle = skin.get(TextField.TextFieldStyle.class);

        blue = new Color(0, 0, 1, 1);
        red = new Color(1, 0, 0, 1);
        white = new Color(1, 1, 1, 1);
        green = new Color(0, 1, 0, 1);
        yellow = new Color(1, 1, 0, 1);

        cursorColor = white;
        runeColor = green;

        shapeRenderer = new ShapeRenderer();
        player = new Player(Gdx.graphics.getWidth() / 2 - 50, 5, mainStage);

        availableSpells = new ArrayList<>();
        runePositions = new ArrayList<>();
        runeRegex = new StringBuilder();

        availableSpells.addAll(characterDetails.getSpells());

        xPosition = Gdx.input.getX();
        yPosition = Gdx.input.getY();

        currentSpellCopies = new ArrayList<>();
        spellBuildup = new MagicBurst[2];
    }

    private void drawTouchpad() {
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();

        Texture padKnobTex = new Texture(Gdx.files.internal("assets/joystick-knob.png"));
        TextureRegion padKnobReg = new TextureRegion(padKnobTex);
        touchpadStyle.knob = new TextureRegionDrawable(padKnobReg);

        Texture padBackTex = new Texture(Gdx.files.internal("assets/joystick-background.png"));
        TextureRegion padBackReg = new TextureRegion(padBackTex);
        touchpadStyle.background = new TextureRegionDrawable(padBackReg);

        touchpad = new Touchpad(5, touchpadStyle);
        touchpad.setPosition(5, 5);
        uiStage.addActor(touchpad);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        //touchpad.debug();
        //settingsTable.debug();

        float frameX = player.getX() - 25;
        float frameY = player.getY() + player.getHeight() + 10;
        drawHpAndManaBars(frameX, frameY);
        if (player.getNameLabel() == null) {
            Label nameLabel = new Label(characterDetails.getNickname(), skin);
            nameLabel.setPosition(frameX, frameY + 35);
            nameLabel.setColor(Color.WHITE);
            player.setNameLabel(nameLabel);
            mainStage.addActor(nameLabel);
        } else {
            player.getNameLabel().setPosition(frameX, frameY + 35);
        }

        Array<Actor> actors = mainStage.getActors();
        for (Actor actor : actors) {
            if (GlobalConfig.actorBoundariesOn) {
                drawActorBoundary(actor);
            }
            if (actor instanceof BaseEnemy) {
                BaseEnemy enemy = (BaseEnemy) actor;
                frameX = enemy.getX() - 10;
                frameY = enemy.getY() + enemy.getHeight() - 25;
                if (enemy.isMagical()) {
                    drawBar(frameX, frameX + 0.5f, frameY, frameY + 0.5f, blue, enemy.manaLeft());
                    frameY += 15;
                }
                drawBar(frameX, frameX + 0.5f, frameY, frameY + 0.5f, red, enemy.hpLeft());
                if (enemy.getNameLabel() == null) {
                    Label nameLabel = new Label(enemy.getName(), skin);
                    nameLabel.setPosition(frameX, frameY + 25);
                    nameLabel.setColor(Color.WHITE);
                    enemy.setNameLabel(nameLabel);
                    mainStage.addActor(nameLabel);
                } else {
                    enemy.getNameLabel().setPosition(frameX, frameY + 25);
                }
            }
        }

        if (Gdx.app.getType() == Application.ApplicationType.Desktop && !inTouchpadRange(xPosition, yPosition) && !inSettingsRange(xPosition, yPosition)) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(cursorColor);
            shapeRenderer.circle(xPosition, yPosition, 5);
            shapeRenderer.end();
        }

        for (Pair runePosition : runePositions) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(runeColor);
            shapeRenderer.circle(runePosition.x, runePosition.y, 7);
            shapeRenderer.end();
        }
    }

    private boolean inTouchpadRange(float x, float y) {
        return touchpadOn && x <= touchpad.getX() + touchpad.getWidth() + 15 && y <= touchpad.getY() + touchpad.getHeight() + 15;
    }

    private boolean inSettingsRange(float x, float y) {
        settingsTable.pack();
        return (x >= settingsTable.getX() && x <= settingsTable.getX() + settingsTable.getWidth()) && (y >= settingsTable.getY() && y <= settingsTable.getY() + settingsTable.getHeight());
    }

    private void drawActorBoundary(Actor actor) {
        if (actor instanceof BaseActor) {
            BaseActor baseActor = (BaseActor) actor;
            if (baseActor.isDrawable()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(white);
                shapeRenderer.polygon(baseActor.getBoundaryPolygon().getTransformedVertices());
                shapeRenderer.end();
            }
        }
    }

    private void drawHpAndManaBars(float frameX, float frameY) {
        drawBar(frameX, frameX + 0.5f, frameY, frameY + 0.5f, blue, player.manaLeft());
        drawBar(frameX, frameX + 0.5f, frameY + 15, frameY + 15.5f, red, player.hpLeft());
    }

    private void drawBar(float x1, float x2, float y1, float y2, Color color, float value) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(white);
        shapeRenderer.rect(x1, y1, 200, 15);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x2, y2, 199 * value, 14);
        shapeRenderer.end();
    }

    protected void castSpell(Pair[] edgePoints) {
        Spell spell;
        int size = runePositions.size();
        float mana = player.getMana();
        if (availableSpells.contains(SpellEnum.BLINK)) {
            if (mana >= Blink.MANA_COST && size > 25 && Blink.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new Blink(0, 0, null);
                setCurrentSpell(spell);
                return;
            }
        }
        if (availableSpells.contains(SpellEnum.MIRROR_IMAGES)) {
            if (size >= 200 && MirrorImages.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new MirrorImages(mainStage, player);
                initSpell(spell);
                return;
            }
        }
        if (size >= 500 && availableSpells.contains(SpellEnum.METEOR_RAIN)) {
            if (mana >= MeteorRain.MANA_COST && MeteorRain.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new MeteorRain(400, 400, mainStage);
                initSpell(spell);
                return;
            }
        }
        if (size >= 500 && availableSpells.contains(SpellEnum.BLIZZARD)) {
            if (mana >= Blizzard.MANA_COST && Blizzard.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new Blizzard(400, 400, mainStage);
                initSpell(spell);
                return;
            }
        }
        if (availableSpells.contains(SpellEnum.FIREBOLT)) {
            if (mana >= Fireball.MANA_COST && size > 10 && Fireball.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = null;
                if (mana >= Fireball.MANA_COST && (size < 50 || !availableSpells.contains(SpellEnum.FIREBALL))) {
                    System.out.println("FIREBOLT CASTED!!");
                    spell = new Fireball(0, 0, null, SPELL_LEVEL_1, 60);
                } else if (mana >= Fireball.MANA_COST * 2 && (size < 100 || !availableSpells.contains(SpellEnum.FIREBOULDER))) {
                    System.out.println("FIREBALL CASTED!!");
                    spell = new Fireball(0, 0, null, SPELL_LEVEL_2, 45);
                } else if (mana >= Fireball.MANA_COST * 3) {
                    System.out.println("FIREBOULDER CASTED!!");
                    spell = new Fireball(0, 0, null, SPELL_LEVEL_3, 45);
                }

                if (spell != null) {
                    setCurrentSpell(spell);
                    for (Mirror mirror : player.getMirrors()) {
                        //currentSpellCopies.add(new )
                    }
                }
                return;
            } else {
                System.out.println("SPELL FAILED");
            }
        }
        if (mana >= Icespike.MANA_COST && size > 30 && availableSpells.contains(SpellEnum.ICE_SPIKE)) {
            if (Icespike.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new Icespike(0, 0, null);
                setCurrentSpell(spell);
                System.out.println("ICE SPIKE CASTED!!");
                return;
            } else {
                System.out.println("SPELL FAILED");
            }
        }
        if (mana >= FrostNova.MANA_COST && size > 50 && availableSpells.contains(SpellEnum.FROST_NOVA)) {
            if (FrostNova.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new FrostNova(0, 0, null);
                setCurrentSpell(spell);
                System.out.println("FROST NOVA CASTED!!");
                return;
            } else {
                System.out.println("SPELL FAILED");
            }
        }
        if (mana >= Tornado.MANA_COST && size > 25 && availableSpells.contains(SpellEnum.TORNADO)) {
            if (Tornado.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new Tornado(0, 0, null);
                setCurrentSpell(spell);
                System.out.println("TORNADO CASTED!!");
                return;
            } else {
                System.out.println("SPELL FAILED");
            }
        }
    }

    private void setCurrentSpell(Spell spell) {
        spellBuildup[0] = new MagicBurst(mainStage, spell.getSchool(), SpellDirection.LEFT, player);
        spellBuildup[1] = new MagicBurst(mainStage, spell.getSchool(), SpellDirection.RIGHT, player);

        spellCasted = true;
        cursorColor = blue;
        currentSpell = spell;
        spell.centerAtActor(player);

        initSpell(spell);
    }

    private void initSpell(Spell spell) {
        player.addMana(-spell.getManaCost());
        for (Spell subspell : spell.getSubspells()) {
            calculateSpellDmg(subspell);
        }
        calculateSpellDmg(spell);
    }

    private void calculateSpellDmg(Spell spell) {
        if (spell.isHarmful()) {
            float dmg = GenericUtil.random.nextInt((int) spell.getMaxDmg()) + spell.getMinDmg();
            boolean isCrit = GenericUtil.random.nextInt(3) > 1;
            if (isCrit) {
                spell.setCrit(true);
                dmg *= 2;
            }
            spell.setDmg(dmg);
        }
    }

    @Override
    public void update(float dt) {
        if (touchpadOn) {
            Vector2 direction = new Vector2(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
            float length = direction.len();

            if (length > 0) {
                System.out.println("TOUCHPAD");
                Direction playerDirection = direction.x > 0 ? Direction.RIGHT : Direction.LEFT;
                player.move(null, playerDirection, direction.x * 4.5f, direction.y * 4.5f);
            }
        }

        Array<Actor> actors = mainStage.getActors();

        for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor.getStage() != null && actor instanceof Spell) {
                Spell spell = (Spell) actor;
                if (spell.isHarmful()) {
                    for (int j = 0; j < actors.size; j++) {
                        Actor secondActor = actors.get(j);
                        if (secondActor instanceof BaseEnemy || (secondActor instanceof EnvironmentActor && !spell.isPenetrable())) {
                            BaseActor baseActor = (BaseActor) secondActor;
                            if (spell.overlaps(baseActor)) {
                                handleSpellHit(spell, baseActor);
                            }
                        }
                    }
                }
            } /*else if(actor.getStage() != null && actor instanceof EnemySpell) {
                EnemySpell enemySpell = (EnemySpell) actor;
                if (enemySpell.isHarmful()) {
                    for (int j = 0; j < actors.size; j++) {
                        Actor secondActor = actors.get(j);
                        if (secondActor instanceof Player || (secondActor instanceof EnvironmentActor && !enemySpell.isPenetrable())) {
                            Player player = (Player) secondActor;
                            if (enemySpell.overlaps(player)) {
                                handleSpellHit(enemySpell, player);
                            }
                        }
                    }
                }
            }*/
        }
    }

    private void handleSpellHit(Spell spell, BaseActor secondActor) {
        if (spell.getStage() == null) {
            return;
        }
        if (secondActor instanceof BaseEnemy || secondActor instanceof EnvironmentActor) {
            if (secondActor instanceof Mage && ((Mage) secondActor).getType() == MageType.FLAMETHROWER) {
                hitMage(spell, secondActor);
            }
            if (spell.isHarmful() && spell.getStage() != null && secondActor.getStage() != null) {
                makeExplosion(spell, secondActor);

                if (secondActor instanceof BaseEnemy) {
                    damageEnemy(spell, (BaseEnemy) secondActor);
                }
            }
        }
    }

/*    private void handleEnemySpellHit(EnemySpell spell, BaseActor secondActor) {
        if (spell.getStage() == null) {
            return;
        }
        if (secondActor instanceof Player || secondActor instanceof EnvironmentActor) {
            if (spell.isHarmful() && spell.getStage() != null && secondActor.getStage() != null) {
                makeExplosion(spell, secondActor);

                if (secondActor instanceof BaseEnemy) {
                    damageEnemy(spell, (BaseEnemy) secondActor);
                }
            }
        }
    }*/

    private void damageEnemy(Spell spell, BaseEnemy enemy) {
        if (spell.getSchool() == SpellSchool.ICE) {
            if (spell.isFreezing()) {
                enemy.setColor(Color.SKY);
                enemy.setSpeed(0);
                //  Ice ice = new Ice(0, 0, mainStage, enemy);
            } else {
                enemy.setColor(Color.SKY);
                float speed = enemy.getSpeed();
                if (speed > enemy.getBaseSpeed() * 0.5f) {
                    enemy.setSpeed(enemy.getBaseSpeed() * 0.5f);
                }
            }
        }
        Direction direction;
        if ((enemy.getX() + enemy.getWidth() / 2) < Gdx.graphics.getWidth() / 2f) {
            direction = Direction.LEFT;
        } else {
            direction = Direction.RIGHT;
        }
        DamageText damageText = new DamageText("" + spell.getDmg(), skin, direction, spell.isCrit());
        damageText.setPosition(enemy.getX() + 50, enemy.getY() + enemy.getHeight() / 2);

        damageText.addAction(Actions.delay(1));
        damageText.addAction(Actions.after(Actions.fadeOut(0.5f)));
        damageText.addAction(Actions.after(Actions.removeActor()));
        mainStage.addActor(damageText);

        if (!enemy.isImmortal()) {
            enemy.setHp(spell.getDmg() > enemy.getHp() ? 0 : enemy.getHp() - spell.getDmg());
        }
        if (enemy.getHp() <= 0) {
            enemy.getNameLabel().remove();
            enemy.setAnimation(enemy.getDeathAnimation());
            enemy.setSpeed(0);
            //enemy.remove();
        } else {
            enemy.setAnimation(enemy.getHitAnimation());
        }
    }

    private void makeExplosion(Spell spell, BaseActor obstacle) {
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
        if (spell instanceof FrostNova) {
            spell.setHarmful(false);
            return;
        }
        spell.remove();
    }

    private void hitMage(Spell spell, BaseActor mage) {
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
        }
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float y = Gdx.graphics.getHeight() - screenY;
        if (inTouchpadRange(screenX, y) || inSettingsRange(screenX, y)) {
            return false;
        }
        //    System.out.println("X: " + screenX + ", Y: " + screenY + ", pointer: " + pointer + ", btn: " + button);
        //   System.out.println("(b)X: " + backButton.getX() + ", Y: " + (GAME_HEIGHT - backButton.getY()) + ", X2: " + (backButton.getX() + backButton.getWidth()) + ", Y2: " + (GAME_HEIGHT - backButton.getY() + backButton.getHeight()));

        if (spellCasted) {
            if (currentSpell instanceof Blink) {
                Blink blink = new Blink(0, 0, mainStage);
                blink.centerAtActor(player);
                currentSpell.centerAtPosition(screenX, y);
                player.centerAtPosition(screenX, y);
            } else if (currentSpell instanceof FrostNova) {
                currentSpell.centerAtPosition(screenX, y);
                ((FrostNova) currentSpell).init(mainStage);
            } else {
                float playerX = player.getX() + player.getWidth() / 2;
                float playerY = player.getY() + player.getHeight() / 2;
                // float cursorY = Gdx.graphics.getHeight()-screenY;//GAME_HEIGHT - screenY;
                double angle = Math.atan2(y - playerY, screenX - playerX);
                angle = Math.toDegrees(angle);
                currentSpell.centerAtActor(player);
                currentSpell.setMotionAngle((float) angle);
                currentSpell.setRotation((float) angle);
            }
            mainStage.addActor(currentSpell);
            for (Spell spell : spellBuildup) {
                spell.remove();
            }
            cursorColor = white;
            return false;
        }

 /*       if (screenX >= this.backButton.getX() && screenX <= this.backButton.getX() + this.backButton.getWidth()
                && screenY <= GAME_HEIGHT - this.backButton.getY() && screenY >= GAME_HEIGHT - this.backButton.getY() - this.backButton.getHeight()) {
            game.setScreen(new MenuScreen(game));
        }*/

        this.runeColor = green;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float y = Gdx.graphics.getHeight() - screenY;
        if (inTouchpadRange(screenX, y) || inSettingsRange(screenX, y)) {
            return false;
        }
        if (spellCasted) {
            spellCasted = false;
            return false;
        }
        int size = runePositions.size();
        if (size < 5) {
            runePositions.clear();
            return false;
        }

        Pair[] edgePoints = new Pair[5];
        int idx = size / 5;
        edgePoints[0] = runePositions.get(0);
        edgePoints[1] = runePositions.get(idx);
        edgePoints[2] = runePositions.get(idx * 2);
        edgePoints[3] = runePositions.get(idx * 3);
        edgePoints[4] = runePositions.get(size - 1);
        castSpell(edgePoints);
        runeRegex = new StringBuilder();
        runePositions.clear();
        previousPosition = null;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float y = Gdx.graphics.getHeight() - screenY;
        if (inTouchpadRange(screenX, y) || inSettingsRange(screenX, y)) {
            return false;
        }
        if (spellCasted) {
            spellCasted = false;
            return false;
        }
        Pair position = new Pair(screenX, y);//GAME_HEIGHT - screenY);
        if (previousPosition == null) {
            previousPosition = position;
        } else {
            float previousX = previousPosition.x;
            float previousY = previousPosition.y;

            if (position.x > previousX) {
                runeRegex.append("R");
            } else if (position.x < previousX) {
                runeRegex.append("L");
            }

            if (position.y > previousY) {
                runeRegex.append("U");
            } else if (position.y < previousY) {
                runeRegex.append("D");
            }
        }

        runePositions.add(position);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        xPosition = screenX;
        yPosition = Gdx.graphics.getHeight() - screenY; //GAME_HEIGHT - screenY;
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
