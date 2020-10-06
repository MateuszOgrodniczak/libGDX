package com.matigames.config;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class GlobalConfig {
    public static final int GAME_WIDTH = 1200;
    public static final int GAME_HEIGHT = 900;

    public static final boolean actorBoundariesOn = false;
    public static final Application.ApplicationType APP_TYPE = Gdx.app.getType();
}
