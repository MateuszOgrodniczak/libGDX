package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matigames.character.CharacterDetails;
import com.matigames.game.WizardsGame;

public abstract class BaseScreen implements Screen, InputProcessor {
    //settings
    public static final float SETTINGS_X = Gdx.graphics.getWidth()*0.85f;
    public static final float SETTINGS_Y = Gdx.graphics.getHeight()*0.95f;
    protected Table settingsTable;

    protected Skin skin;
    protected Game game;
    protected CharacterDetails characterDetails;
    protected Stage mainStage;
    protected Stage uiStage;

    protected TextButton backButton;

    public BaseScreen(Game game) {
        this.game = game;
        mainStage = new Stage();
        uiStage = new Stage();

        initialize();
    }

    public void initialize() {
        characterDetails = ((WizardsGame) game).getCharacterDetails();
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        settingsTable = new Table();
        settingsTable.setPosition(SETTINGS_X, SETTINGS_Y);

        backButton = new TextButton("Back to game menu", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        settingsTable.add(backButton);
        uiStage.addActor(settingsTable);
    }

    public abstract void update(float dt);

    public void render(float dt) {
        mainStage.act(dt);
        uiStage.act(dt);
        update(dt);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mainStage.draw();
        uiStage.draw();
    }

    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
