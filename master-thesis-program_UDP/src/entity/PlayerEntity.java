package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerEntity implements Serializable {
    private int id;
    private float x;
    private float y;
    private float rotation;
    private float motionAngle;
    private int shieldPower;
    private boolean destroyed;
    private boolean removed;

    private List<LaserEntity> lasers = new ArrayList<>();

    private boolean thrustersVisible;

    public PlayerEntity() {
    }

    public PlayerEntity(float x, float y, float rotation, float motionAngle, int shieldPower) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.motionAngle = motionAngle;
        this.shieldPower = shieldPower;
    }

    public String toString() {
        return "PlayerEntity: id=" + id + ", x=" + x + ", y=" + y + ", rot=" + rotation + ", angle=" + motionAngle + ", shield=" + shieldPower;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getShieldPower() {
        return shieldPower;
    }

    public void setShieldPower(int shieldPower) {
        this.shieldPower = shieldPower;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getMotionAngle() {
        return motionAngle;
    }

    public void setMotionAngle(float motionAngle) {
        this.motionAngle = motionAngle;
    }


    public boolean isThrustersVisible() {
        return thrustersVisible;
    }

    public void setThrustersVisible(boolean thrustersVisible) {
        this.thrustersVisible = thrustersVisible;
    }


    public List<LaserEntity> getLasers() {
        return lasers;
    }

    public void setLasers(List<LaserEntity> lasers) {
        this.lasers = lasers;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
