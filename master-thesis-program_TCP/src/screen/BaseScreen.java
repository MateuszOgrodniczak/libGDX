package screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;

import static game.SpaceGame.GAME_HEIGHT;
import static game.SpaceGame.GAME_WIDTH;

public abstract class BaseScreen implements Screen, InputProcessor {
    protected ShapeRenderer shapeRenderer;
    protected Stage mainStage;
    protected Stage uiStage;


    private static final int CHAT_HEIGHT = 850;
    private static final int CHAT_WIDTH = 250;

    public BaseScreen() {
        shapeRenderer = new ShapeRenderer();
        mainStage = new Stage();
        uiStage = new Stage();
        initialize();
    }

    public abstract void initialize();

    public abstract void update(float dt);

    public void render(float dt) {
        mainStage.act(dt);
        uiStage.act(dt);
        update(dt);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Rectangle chatBox = new Rectangle(GAME_WIDTH - CHAT_WIDTH, GAME_HEIGHT - CHAT_HEIGHT, CHAT_WIDTH, CHAT_HEIGHT);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 0.5f);
        shapeRenderer.rect(chatBox.getX(), chatBox.getY(), chatBox.getWidth(), chatBox.getHeight());
        shapeRenderer.end();

        mainStage.draw();
        uiStage.draw();
    }

    // methods required by Screen interface
    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
    }

    public void show() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(this);
        im.addProcessor(uiStage);
        im.addProcessor(mainStage);
    }

    public void hide() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        im.removeProcessor(uiStage);
        im.removeProcessor(mainStage);
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

    @Override
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
