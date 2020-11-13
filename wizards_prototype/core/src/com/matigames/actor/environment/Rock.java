package com.matigames.actor.environment;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.BaseActor;

public class Rock extends EnvironmentActor {

    public Rock(float x, float y, Stage s) {
        super(x, y, s);

        loadTexture("assets/rock.png");
        setBoundaryPolygon(8, 1, 1);
    }
}
