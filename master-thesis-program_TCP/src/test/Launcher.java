package test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import game.SpaceGame;

import static game.SpaceGame.GAME_HEIGHT;
import static game.SpaceGame.GAME_WIDTH;

public class Launcher {

    public static void main(String... args) {
        Game game = new SpaceGame();
        new LwjglApplication(game, "Space shooter", GAME_WIDTH, GAME_HEIGHT);
    }
}
