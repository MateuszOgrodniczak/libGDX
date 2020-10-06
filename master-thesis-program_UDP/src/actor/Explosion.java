package actor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class Explosion extends BaseActor {

    public Explosion(float x, float y, Stage s) {
        super(x, y, s);

        loadAnimationFromSheet("assets/explosion.png", 6, 6, 0.05f, true);

        addAction(Actions.delay(1));
        addAction(Actions.after(Actions.fadeOut(0.5f)));
        addAction(Actions.after(Actions.removeActor()));
    }
}
