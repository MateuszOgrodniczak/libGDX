package actor;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class SpaceshipEnemy extends BaseActor {
    private Thrusters thrusters;
    private Shield shield;
    private int shieldPower;

    public SpaceshipEnemy(float x, float y, Stage s) {
        super(x, y, s);

        loadTexture("assets/spaceship.png");
        setColor(255, 0, 0, 1);
        setBoundaryPolygon(8);

        thrusters = new Thrusters(0, 0, s);
        addActor(thrusters);
        thrusters.setPosition(-thrusters.getWidth(), getHeight() / 2 - thrusters.getHeight() / 2);

        shield = new Shield(0, 0, s);
        addActor(shield);
        shield.setPosition(getWidth() / 2 - shield.getWidth() / 2, getHeight() / 2 - shield.getHeight() / 2);
        shieldPower = 100;
        // shield.centerAtActor(this);

        setAcceleration(200);
        setMaxSpeed(100);
        setDeceleration(10);
    }

    public Thrusters getThrusters() {
        return thrusters;
    }

    public int getShieldPower() {
        return shieldPower;
    }

    public void setShieldPower(int power) {
        shieldPower = power;
    }

    public boolean areThrustersVisible() {
        return thrusters.isVisible();
    }

    public void setThrustersVisible(boolean visible) {
        thrusters.setVisible(visible);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        shield.setOpacity(shieldPower / 100f);
        if (shieldPower <= 0) {
            shield.setVisible(false);
        }

        applyPhysics(delta);
        boundToWorld();
    }
}
