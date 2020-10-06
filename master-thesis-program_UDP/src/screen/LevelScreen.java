package screen;

import actor.BaseActor;
import actor.LaserEnemy;
import actor.Spaceship;
import actor.SpaceshipEnemy;
import entity.LaserEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static game.SpaceGame.GAME_HEIGHT;
import static game.SpaceGame.GAME_WIDTH;

public class LevelScreen extends BaseScreen {
    private Spaceship spaceship;
    private Map<Integer, SpaceshipEnemy> enemySpaceships = new HashMap<>();
    private BaseActor space;
    private BaseActor winMessage;
    private boolean win;

    @Override
    public void initialize() {
        space = new BaseActor(0, 0, mainStage);
        space.loadTexture("assets/space.png");
        space.setWidth(GAME_WIDTH);
        space.setHeight(GAME_HEIGHT);
    }

    public Map<Integer, SpaceshipEnemy> getEnemySpaceships() {
        return enemySpaceships;
    }

    public void addEnemySpaceShip(float x, float y, int id) {
        enemySpaceships.put(id, new SpaceshipEnemy(x, y, mainStage));
    }

    public void addLasers(List<LaserEntity> lasers) {
        for (LaserEntity laser : lasers) {
            LaserEnemy laserEnemy = new LaserEnemy(laser.getX(), laser.getY(), mainStage);
            laserEnemy.setRotation(laser.getRotation());
            laserEnemy.setMotionAngle(laser.getMotionAngle());
        }
    }

    public Spaceship addSpaceShip(float x, float y) {
        spaceship = new Spaceship(x, y, mainStage);
        return spaceship;
    }

    @Override
    public void update(float delta) {
        if (spaceship != null && spaceship.isDestroyed()) {
            return;
        }
    }

}
