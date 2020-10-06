package actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import entity.LaserEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static game.SpaceGame.GAME_HEIGHT;
import static game.SpaceGame.GAME_WIDTH;

public class Spaceship extends BaseActor {
    private static final int BOTTOM_VALUE = 50;

    private Thrusters thrusters;
    private Shield shield;
    private int shieldPower;
    private boolean destroyed;
    private Random random = new Random();

    private final List<Laser> lasers = Collections.synchronizedList(new ArrayList<>()); //new ArrayList<>();

    public Spaceship(float x, float y, Stage s) {
        super(x, y, s);

        loadTexture("assets/spaceship.png");
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

    public List<LaserEntity> getNewLasers() {
        if (lasers.size() == 0) return Collections.emptyList();

        List<LaserEntity> laserEntities = new ArrayList<>();
        synchronized (lasers) {
            for (Laser laser : lasers) {
                laserEntities.add(new LaserEntity(laser.getX(), laser.getY(), laser.getRotation(), laser.getMotionAngle()));
            }
        }

        lasers.clear();
        return laserEntities;
    }

    public List<LaserEntity> getLasers() {
        if (lasers.size() == 0) {
            return Collections.emptyList();
        }

        List<Laser> toRemove = new ArrayList<>();
        lasers.forEach(laser -> {
            if (laser.getStage() == null) {
                toRemove.add(laser);
            }
        });

        lasers.removeAll(toRemove);

        List<LaserEntity> laserEntities = new ArrayList<>();
        synchronized (lasers) {
            for (Laser laser : lasers) {
                laserEntities.add(new LaserEntity(laser.getX(), laser.getY(), laser.getRotation(), laser.getMotionAngle()));
            }
        }
        return laserEntities;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        float degreesPerSecond = 120;

        float x = getX();
        float y = getY();
        if (x > GAME_WIDTH - 100 || x < BOTTOM_VALUE || y > GAME_HEIGHT - 100 || y < BOTTOM_VALUE) {
            rotateBy(degreesPerSecond * delta);
            accelerateAtAngle(getRotation());
            thrusters.setVisible(true);
        }

        int result = random.nextInt(100);
        if (result > 70) {
            //do nothing
        } else if (result > 50) {
            accelerateAtAngle(getRotation());
            thrusters.setVisible(true);
        } else if (result > 30) {
            rotateBy(degreesPerSecond * delta);
        } else if (result > 10) {
            rotateBy(-degreesPerSecond * delta);
        } else {
            createLaser();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rotateBy(degreesPerSecond * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rotateBy(-degreesPerSecond * delta);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            createLaser();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            accelerateAtAngle(getRotation());
            thrusters.setVisible(true);
        } else {
            thrusters.setVisible(false);
        }

        shield.setOpacity(shieldPower / 100f);
        if (shieldPower <= 0) {
            shield.setVisible(false);
        }

        applyPhysics(delta);
        boundToWorld();
    }

    private void createLaser() {
        Laser laser = new Laser(0, 0, this.getStage());

        laser.centerAtActor(this);
        laser.setRotation(getRotation());

        laser.setMotionAngle(getRotation());
        lasers.add(laser);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
