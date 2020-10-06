package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matigames.character.CharacterDetails;
import com.matigames.game.WizardsGame;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;

public abstract class BaseScreen implements Screen, InputProcessor {
    private static final float BUTTON_WIDTH = Gdx.graphics.getWidth() * 0.15f;
    private static final float BUTTON_HEIGHT = Gdx.graphics.getHeight() * 0.05f;
    protected Skin skin;
    protected Game game;
    protected CharacterDetails characterDetails;
    protected Stage mainStage;
    protected Stage uiStage;

    protected TextButton exitButton;

    public BaseScreen(Game game) {
        this.game = game;
        mainStage = new Stage();
        uiStage = new Stage();

        initialize();
    }

    public void initialize() {
        characterDetails = ((WizardsGame) game).getCharacterDetails();
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        exitButton = new TextButton("Back to game menu", skin);
        exitButton.setPosition(Gdx.graphics.getWidth()*0.85f, Gdx.graphics.getHeight()*0.95f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        uiStage.addActor(exitButton);
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
