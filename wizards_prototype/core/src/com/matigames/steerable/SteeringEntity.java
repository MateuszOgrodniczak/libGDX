package com.matigames.steerable;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.matigames.actor.BaseActor;

public class SteeringEntity implements Steerable<Vector2> {
    TextureRegion region;
    BaseActor actor;

    private static final SteeringAcceleration<Vector2> steeringOutput =
            new SteeringAcceleration<Vector2>(new Vector2());


    float boundingRadius;
    boolean tagged;

    float maxLinearSpeed;
    float maxLinearAcceleration;
    float maxAngularSpeed;
    float maxAngularAcceleration;

    Vector2 position;
    float orientation;
    Vector2 linearVelocity;
    float angularVelocity;
    float maxSpeed;
    boolean independentFacing;
    SteeringBehavior<Vector2> steeringBehavior;

    public SteeringEntity(BaseActor actor, boolean independentFacing, float boundingRadius) {
        //   this.region = region;
        this.actor = actor;
        this.independentFacing = independentFacing;
        this.boundingRadius = boundingRadius;
        this.tagged = false;

        setPosition(new Vector2(actor.getX(), actor.getY()));
    }

    public static SteeringAcceleration<Vector2> getSteeringOutput() {
        return steeringOutput;
    }

    public TextureRegion getRegion() {
        return region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getMaxLinearSpeed() {
        return actor.getSpeed();
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return actor.getVelocityVec();
    }

    public void setLinearVelocity(Vector2 linearVelocity) {
        this.linearVelocity = actor.getVelocityVec();
    }

    @Override
    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public boolean isIndependentFacing() {
        return independentFacing;
    }

    public void setIndependentFacing(boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

    public SteeringBehavior<Vector2> getSteeringBehavior() {
        return steeringBehavior;
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    /* Here you should implement missing methods inherited from Steerable */

    @Override
    public Vector2 getPosition() {
        return new Vector2(actor.getX(), actor.getY());
    }

    @Override
    public float getOrientation() {
        return actor.getMotionAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        //???  actor.setTransform(getPosition(), orientation);
    }

    // Actual implementation depends on your coordinate system.
    // Here we assume the y-axis is pointing upwards.
    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(-vector.x, vector.y);
    }

    // Actual implementation depends on your coordinate system.
    // Here we assume the y-axis is pointing upwards.
    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float) Math.sin(angle);
        outVector.y = (float) Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return null;
    }

    public void update(float delta) {
        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            /*
             * Here you might want to add a motor control layer filtering steering accelerations.
             *
             * For instance, a car in a driving game has physical constraints on its movement:
             * - it cannot turn while stationary
             * - the faster it moves, the slower it can turn (without going into a skid)
             * - it can brake much more quickly than it can accelerate
             * - it only moves in the direction it is facing (ignoring power slides)
             */

            // Apply steering acceleration to move this agent
            applySteering(steeringOutput, delta);
        }
    }

    private void applySteering(SteeringAcceleration<Vector2> steering, float time) {
        // Update position and linear velocity. Velocity is trimmed to maximum speed
        if (linearVelocity == null) {
            linearVelocity = actor.getVelocityVec();
        }
        this.position.mulAdd(linearVelocity, time);
        this.linearVelocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed());

        // Update orientation and angular velocity
        if (independentFacing) {
            this.orientation += angularVelocity * time;
            this.angularVelocity += steering.angular * time;
        } else {
            // For non-independent facing we have to align orientation to linear velocity
            float newOrientation = calculateOrientationFromLinearVelocity(this);
            if (newOrientation != this.orientation) {
                this.angularVelocity = (newOrientation - this.orientation) * time;
                this.orientation = newOrientation;
            }
        }
    }

    public static float calculateOrientationFromLinearVelocity(Steerable<Vector2> character) {
        // If we haven't got any velocity, then we can do nothing.
        if (character.getLinearVelocity().isZero(character.getZeroLinearSpeedThreshold()))
            return character.getOrientation();

        return character.vectorToAngle(character.getLinearVelocity());
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }
}
