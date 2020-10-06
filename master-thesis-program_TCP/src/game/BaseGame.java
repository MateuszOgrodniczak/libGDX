package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;

public abstract class BaseGame extends Game {

    @Override
    public void create() {
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        //initialize();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        update(delta);
    }

    // public abstract void initialize();

    public abstract void update(float delta);
}
