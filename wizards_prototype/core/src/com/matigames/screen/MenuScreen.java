package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matigames.actor.enemy.MageType;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class MenuScreen extends BaseScreen {
    private static final float BUTTON_WIDTH = Gdx.graphics.getWidth() * 0.3f;
    private static final float BUTTON_HEIGHT = Gdx.graphics.getHeight() * 0.05f;
    private static final float BUTTON_PAD_TOP = Gdx.graphics.getHeight() * 0.01f;

    public MenuScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();
        backButton.setVisible(false);

        Table menuTable = new Table();
        menuTable.debugTable();
        menuTable.setTouchable(Touchable.enabled);
        menuTable.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        TextButton trainingScreenSwitch = new TextButton("Training mode", skin);
        trainingScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TrainingScreen(game));
            }
        });

        TextButton tower1ScreenSwitch = new TextButton("Tower mode: level 1", skin);
        tower1ScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TowerScreen(game, MageType.WILLY));
            }
        });

        TextButton tower2ScreenSwitch = new TextButton("Tower mode: level 2", skin);
        tower2ScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TowerScreen(game, MageType.APPRENTICE));
            }
        });

        TextButton tower3ScreenSwitch = new TextButton("Tower mode: level 3", skin);
        tower3ScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TowerScreen(game, MageType.FLAMETHROWER));
            }
        });

        TextButton tower4ScreenSwitch = new TextButton("Tower mode: level 4", skin);
        tower4ScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TowerScreen(game, MageType.IMP_MASTER));
            }
        });

        TextButton tower5ScreenSwitch = new TextButton("Tower mode: level 5", skin);
        tower5ScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new TowerScreen(game, MageType.PRIEST));
            }
        });

        TextButton talentsScreenSwitch = new TextButton("Talent trees", skin);
        talentsScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new CharacterScreen(game));
            }
        });

        TextButton spellbookScreenSwitch = new TextButton("Spellbook", skin);
        spellbookScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new SpellbookScreen(game));
            }
        });

        TextButton dungeonScreenSwitch = new TextButton("Dungeon: Level 1", skin);
        dungeonScreenSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("CLICKED");
                game.setScreen(new DungeonScreen(game));
            }
        });

        menuTable.add(trainingScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
        menuTable.row();
        menuTable.add(tower1ScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(tower2ScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(tower3ScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(tower4ScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(tower5ScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(talentsScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(spellbookScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);
        menuTable.row();
        menuTable.add(dungeonScreenSwitch).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(BUTTON_PAD_TOP);

        uiStage.addActor(menuTable);
    }

    @Override
    public void show() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(this);
        im.addProcessor(uiStage);
    }

    @Override
    public void hide() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        im.removeProcessor(uiStage);
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyPressed(Input.Keys.T)) {
            game.setScreen(new CharacterScreen(game));
        } else if (Gdx.input.isKeyPressed(Input.Keys.B)) {
            game.setScreen(new SpellbookScreen(game));
        } else if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            game.setScreen(new TrainingScreen(game));
        }
    }
}
