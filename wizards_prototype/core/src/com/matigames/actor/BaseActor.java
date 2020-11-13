package com.matigames.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.graphics.Texture.TextureFilter;
import static com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class BaseActor extends Group {
    private Animation<TextureRegion> animation;
    private float elapsedTime;
    private boolean animationPaused;
    private float animationTime;

    private Vector2 velocityVec = new Vector2(0, 0);
    private Vector2 accelerationVec = new Vector2(0, 0);
    private float acceleration;

    private float maxSpeed;
    protected float baseSpeed;
    private float deceleration;

    private Polygon boundaryPolygon;

    protected boolean isFlipped;
    protected Direction direction;

    private Label nameLabel;

    protected boolean drawable = true;

    public boolean isDrawable() {
        return drawable;
    }

    public BaseActor(float x, float y, Stage s) {
        super();
        setPosition(x, y);

        acceleration = 400;
        maxSpeed = 100;
        deceleration = 400;

        if (s != null) {
            s.addActor(this);
        }
    }

    public void alignCamera() {
        Camera camera = this.getStage().getCamera();

        //center camera at actor
        camera.position.set(getX() + getOriginX(), getY() + getOriginY(), 0);

        //bound camera to layout
        camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth / 2, GAME_WIDTH - camera.viewportWidth / 2);
        camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2, GAME_HEIGHT - camera.viewportHeight / 2);
        camera.update();
    }

    public void boundToWorld() {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        if (x < 0) {
            setX(0);
        } else if (x + width > Gdx.graphics.getWidth()) {
            setX(Gdx.graphics.getWidth() - getWidth());
        }
        if (y < 0) {
            setY(0);
        } else if (y + height > Gdx.graphics.getHeight()) {
            setY(Gdx.graphics.getHeight() - getHeight());
        }
    }

    public Vector2 preventOverlap(BaseActor other) {
        Polygon thisPolygon = this.getBoundaryPolygon();
        Polygon otherPolygon = other.getBoundaryPolygon();

        if (!thisPolygon.getBoundingRectangle().overlaps(otherPolygon.getBoundingRectangle())) {
            return null;
        }

        MinimumTranslationVector mtv = new MinimumTranslationVector();
        boolean overlaps = Intersector.overlapConvexPolygons(thisPolygon, otherPolygon, mtv);

        if (!overlaps) {
            return null;
        }

        this.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        return mtv.normal;
    }

    public void centerAtPosition(float x, float y) {
        setPosition(x - getWidth() / 2, y - getHeight() / 2);
    }

    public void centerAtActor(BaseActor other) {
        centerAtPosition(other.getX() + other.getWidth() / 2, other.getY() + other.getHeight() / 2);
    }

    public void setOpacity(float opacity) {
        getColor().a = opacity;
    }

    public void applyPhysics(float dt) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * dt, accelerationVec.y * dt);

        float speed = getSpeed();

        // decrease speed (decelerate) when not accelerating
        if (accelerationVec.len() == 0) {
            speed -= deceleration * dt;
        }

        // keep speed within set bounds
        speed = MathUtils.clamp(speed, 0, maxSpeed);

        // update velocity
        setSpeed(speed);

        // apply velocity
        moveBy(velocityVec.x * dt, velocityVec.y * dt);

        // reset acceleration
        accelerationVec.set(0, 0);
    }

    public void setDirection(float x, float y) {
        velocityVec.x = x;
        velocityVec.y = y;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(float ms) {
        maxSpeed = ms;
    }

    public void setDeceleration(float dec) {
        deceleration = dec;
    }

    public void accelerateAtAngle(float angle) {
        accelerationVec.add(new Vector2(acceleration, 0).setAngle(angle));
    }

    public void accelerateForward() {
        accelerateAtAngle(getRotation());
    }

    public void setSpeed(float speed) {
        if (velocityVec.len() == 0) {
            velocityVec.set(speed, 0);
        } else {
            velocityVec.setLength(speed);
        }
    }

    public float getSpeed() {
        return velocityVec.len();
    }

    public void setMotionAngle(float angle) {
        velocityVec.setAngle(angle);
    }

    public float getMotionAngle() {
        return velocityVec.angle();
    }

    public boolean isMoving() {
        return getSpeed() > 0;
    }

    public void setAnimation(Animation<TextureRegion> animation) {
        if(animation == null) {
            return;
        }
        elapsedTime = 0;
        this.animation = animation;
        TextureRegion tr = animation.getKeyFrame(0);

        float width = tr.getRegionWidth();
        float height = tr.getRegionHeight();
        setSize(width, height);
        setOrigin(width / 2, height / 2);

        if (boundaryPolygon == null) {
            setBoundaryRectangle();
        }
    }

    public void setBoundaryRectangle() {
        float width = getWidth();
        float height = getHeight();

        float[] vertices = {0f, 0f, width, 0f, width, height, 0f, height};
        boundaryPolygon = new Polygon(vertices);
    }

    public void setBoundaryPolygon(int numSides, int widthCrop, int heightCrop) {
        float w = getWidth() / widthCrop;
        float h = getHeight() / heightCrop;
        float[] vertices = new float[2 * numSides];
        for (int i = 0; i < numSides; i++) {
            float angle = i * 6.28f / numSides;
            // x-coordinate
            vertices[2 * i] = w / 2 * MathUtils.cos(angle) + w / 2 * widthCrop;
            // y-coordinate
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(angle) + h / 2 * heightCrop;
        }
        boundaryPolygon = new Polygon(vertices);
    }

    public Polygon getBoundaryPolygon() {
        boundaryPolygon.setPosition(getX(), getY());
        boundaryPolygon.setOrigin(getOriginX(), getOriginY());
        boundaryPolygon.setScale(getScaleX(), getScaleY());
        boundaryPolygon.setRotation(getRotation());
        return boundaryPolygon;
    }

    public boolean overlaps(BaseActor other) {
        Polygon boundaryPolygon = getBoundaryPolygon();
        Polygon otherBoundaryPolygon = other.getBoundaryPolygon();

        if (!boundaryPolygon.getBoundingRectangle().overlaps(otherBoundaryPolygon.getBoundingRectangle())) {
            return false;
        }

        return Intersector.overlapConvexPolygons(boundaryPolygon, otherBoundaryPolygon);
    }

    public boolean overlapsRectangle(BaseActor other) {
        return boundaryPolygon.getBoundingRectangle().overlaps(other.getBoundaryPolygon().getBoundingRectangle());
    }

    public void setAnimationPaused(boolean paused) {
        animationPaused = paused;
    }

    public boolean isAnimationFinished() {
        return animation.isAnimationFinished(elapsedTime);
    }

    public Animation<TextureRegion> loadTexture(String fileName) {
        return loadAnimationFromFiles(new String[]{fileName}, 1, true);
    }

    public Animation<TextureRegion> loadAnimationFromFiles(String[] files, float frameDuration, boolean loop) {
        Array<TextureRegion> textureRegions = new Array<>();

        for (String fileName : files) {
            Texture texture = new Texture(Gdx.files.internal(fileName));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            textureRegions.add(new TextureRegion(texture));
        }

        return obtainTextureRegionAnimation(frameDuration, textureRegions, loop);
    }

    public Animation<TextureRegion> loadAnimationFromSheet(String fileName, int rows, int cols, float frameDuration, boolean loop) {
        Texture texture = new Texture(Gdx.files.internal(fileName), true);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        int width = texture.getWidth() / cols;
        int height = texture.getHeight() / rows;

        TextureRegion[][] temp = new TextureRegion(texture).split(width, height);
        Array<TextureRegion> textureRegions = new Array<>();

        for (TextureRegion[] regionRow : temp) {
            for (TextureRegion region : regionRow) {
                textureRegions.add(region);
            }
        }

        return obtainTextureRegionAnimation(frameDuration, textureRegions, loop);
    }

    private Animation<TextureRegion> obtainTextureRegionAnimation(float frameDuration, Array<TextureRegion> textureRegions, boolean loop) {
        Animation<TextureRegion> animation = new Animation<>(frameDuration, textureRegions);
        if (loop) {
            animation.setPlayMode(Animation.PlayMode.LOOP);
        } else {
            animation.setPlayMode(Animation.PlayMode.NORMAL);
        }

        if (this.animation == null) {
            setAnimation(animation);
        }

        return animation;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!animationPaused) {
            elapsedTime += delta;
            //  animationTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);

        if (animation != null && isVisible()) {
            //  batch.draw(animation.getKeyFrame(elapsedTime), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            boolean flip = direction == Direction.LEFT;
            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();
            //batch.draw(animation.getKeyFrame(elapsedTime), flip ? x+width : x, y, flip ? -width : width, height);
            batch.draw(animation.getKeyFrame(elapsedTime), flip ? x + width : x, y, flip ? getOriginX() - width : getOriginX(), getOriginY(), flip ? -width : width, height, getScaleX(), getScaleY(), getRotation());
        }
        super.draw(batch, parentAlpha);
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(Label nameLabel) {
        this.nameLabel = nameLabel;
    }

    public Vector2 getVelocityVec() {
        return velocityVec;
    }
}
