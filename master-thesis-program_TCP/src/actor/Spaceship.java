package actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Stage;
import entity.LaserEntity;
import global.GlobalConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static game.SpaceGame.*;

public class Spaceship extends BaseActor {
    private Thrusters thrusters;
    private Shield shield;
    private int shieldPower;
    private boolean destroyed;
    private boolean newSoundEffect;
    private Random random = new Random();

    private final List<Laser> lasers = Collections.synchronizedList(new ArrayList<>());

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

    public int lasersCount() {
        return this.lasers.size();
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

    @Override
    public void act(float delta) {
        super.act(delta);

        float degreesPerSecond = 120;

        float x = getX();
        float y = getY();
        if (x > GAME_WIDTH - CHAT_WIDTH - 50 || x < 50 || y > GAME_HEIGHT - 100 || y < 50) {
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
            Laser laser = new Laser(0, 0, this.getStage());

            laser.centerAtActor(this);
            laser.setRotation(getRotation());

            laser.setMotionAngle(getRotation());
            lasers.add(laser);

            if (!GlobalConfig.isMuted) {
                Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                laserSound.play(1.0f);
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rotateBy(degreesPerSecond * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rotateBy(-degreesPerSecond * delta);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Laser laser = new Laser(0, 0, this.getStage());

            laser.centerAtActor(this);
            laser.setRotation(getRotation());

            laser.setMotionAngle(getRotation());
            lasers.add(laser);

            if (!GlobalConfig.isMuted) {
                Sound laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/laser.mp3"));
                laserSound.play(1.0f);
            }
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

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isNewSoundEffect() {
        return newSoundEffect;
    }

    public void setNewSoundEffect(boolean newSoundEffect) {
        this.newSoundEffect = newSoundEffect;
    }
}
