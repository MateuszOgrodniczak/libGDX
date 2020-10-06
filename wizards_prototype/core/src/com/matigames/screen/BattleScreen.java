package com.matigames.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.matigames.actor.Player;
import com.matigames.actor.BaseActor;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.spell.*;
import com.matigames.config.GlobalConfig;
import com.matigames.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public abstract class BattleScreen extends BaseScreen {
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

    protected List<Pair> runePositions;
    protected StringBuilder runeRegex;
    protected boolean spellCasted;
    protected Spell currentSpell;

    //flags
    private static final byte SPELL_LEVEL_1 = 1;
    private static final byte SPELL_LEVEL_2 = 2;
    private static final byte SPELL_LEVEL_3 = 3;

    public BattleScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        blue = new Color(0, 0, 1, 1);
        red = new Color(1, 0, 0, 1);
        white = new Color(1, 1, 1, 1);
        green = new Color(0, 1, 0, 1);

        cursorColor = white;

        shapeRenderer = new ShapeRenderer();
        player = new Player(Gdx.graphics.getWidth() / 2 - 50, 5, mainStage);

        availableSpells = new ArrayList<>();
        runePositions = new ArrayList<>();
        runeRegex = new StringBuilder();

        availableSpells.addAll(characterDetails.getSpells());

        xPosition = Gdx.input.getX();
        yPosition = Gdx.input.getY();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

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
            if(GlobalConfig.actorBoundariesOn) {
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

        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
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

    private void drawActorBoundary(Actor actor) {
        if(actor instanceof BaseActor) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(white);
            shapeRenderer.polygon(((BaseActor) actor).getBoundaryPolygon().getTransformedVertices());
            shapeRenderer.end();
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
        Spell spell = null;
        int size = runePositions.size();
        float mana = player.getMana();
        if (availableSpells.contains(SpellEnum.BLINK)) {
            if (mana >= Blink.MANA_COST && size > 25 && Blink.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                spell = new Blink(0, 0, null);
                setCurrentSpell(spell);
                return;
            }
        }
        if (availableSpells.contains(SpellEnum.FIREBOLT)) {
            if (mana >= Fireball.MANA_COST && size > 10 && Fireball.isAllowed(runeRegex.toString(), 10, edgePoints)) {
                if (size < 50 || !availableSpells.contains(SpellEnum.FIREBALL)) {
                    System.out.println("FIREBOLT CASTED!!");
                    spell = new Fireball(0, 0, null, SPELL_LEVEL_1, 60);
                } else if (size < 100 || !availableSpells.contains(SpellEnum.FIREBOULDER)) {
                    System.out.println("FIREBALL CASTED!!");
                    spell = new Fireball(0, 0, mainStage, SPELL_LEVEL_2, 45);
                } else {
                    System.out.println("FIREBOULDER CASTED!!");
                    spell = new Fireball(0, 0, mainStage, SPELL_LEVEL_3, 45);
                }

                if (mana < spell.getManaCost()) {
                    spell.remove();
                    return;
                }

                setCurrentSpell(spell);
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
        spellCasted = true;
        cursorColor = blue;
        currentSpell = spell;
        spell.centerAtActor(player);
        player.addMana(-spell.getManaCost());
    }

    @Override
    public void update(float dt) {

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
        System.out.println("X: " + screenX + ", Y: " + screenY + ", pointer: " + pointer + ", btn: " + button);
        System.out.println("(b)X: " + exitButton.getX() + ", Y: " + (GAME_HEIGHT - exitButton.getY()) + ", X2: " + (exitButton.getX() + exitButton.getWidth()) + ", Y2: " + (GAME_HEIGHT - exitButton.getY() + exitButton.getHeight()));

        if (spellCasted) {
            if (currentSpell instanceof Blink) {
                Blink blink = new Blink(0, 0, mainStage);
                blink.centerAtActor(player);
                currentSpell.centerAtPosition(screenX, GAME_HEIGHT - screenY);
                player.centerAtPosition(screenX, GAME_HEIGHT - screenY);
            } else {
                float playerX = player.getX() + player.getWidth() / 2;
                float playerY = player.getY() + player.getHeight() / 2;
                float cursorY = Gdx.graphics.getHeight()-screenY;//GAME_HEIGHT - screenY;
                double angle = Math.atan2(cursorY - playerY, screenX - playerX);
                angle = Math.toDegrees(angle);
                currentSpell.centerAtActor(player);
                currentSpell.setMotionAngle((float) angle);
                currentSpell.setRotation((float) angle);
            }
            mainStage.addActor(currentSpell);
            cursorColor = white;
            return false;
        }

        if (screenX >= this.exitButton.getX() && screenX <= this.exitButton.getX() + this.exitButton.getWidth()
                && screenY <= GAME_HEIGHT - this.exitButton.getY() && screenY >= GAME_HEIGHT - this.exitButton.getY() - this.exitButton.getHeight()) {
            game.setScreen(new MenuScreen(game));
        }

        this.runeColor = green;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (spellCasted) {
            spellCasted = false;
            return false;
        }
        int size = runePositions.size();
        if (size < 5) {
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
        if (spellCasted) {
            spellCasted = false;
            return false;
        }
        Pair position = new Pair(screenX, Gdx.graphics.getHeight()-screenY);//GAME_HEIGHT - screenY);
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
        yPosition = Gdx.graphics.getHeight()-screenY; //GAME_HEIGHT - screenY;
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
