package entity;

import java.io.Serializable;

public class LaserEntity implements Serializable {
    private float x;
    private float y;
    private float rotation;
    private float motionAngle;

    public LaserEntity() {
    }

    public LaserEntity(float x, float y, float rotation, float motionAngle) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.motionAngle = motionAngle;
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
}
