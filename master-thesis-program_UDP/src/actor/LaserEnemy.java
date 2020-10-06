package actor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class LaserEnemy extends BaseActor {

    public LaserEnemy(float x, float y, Stage s) {
        super(x, y, s);

        loadTexture("assets/laser.png");

        addAction(Actions.delay(1));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));

        setSpeed(400);
        setMaxSpeed(400);
        setDeceleration(0);

        setColor(255, 0, 0, 1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        applyPhysics(delta);
        boundToWorld();
    }
}
