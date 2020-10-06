package com.matigames.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.matigames.game.WizardsGame;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//new LwjglApplication(new WizardsGame(), config);
		new LwjglApplication(new WizardsGame(), "Prototype", GAME_WIDTH, GAME_HEIGHT);
	}
}
