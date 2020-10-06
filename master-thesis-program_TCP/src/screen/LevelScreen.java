package screen;

import actor.BaseActor;
import actor.LaserEnemy;
import actor.Spaceship;
import actor.SpaceshipEnemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import entity.LaserEntity;
import entity.MessageEntity;
import global.GlobalConfig;

import java.util.List;
import java.util.*;

import static game.SpaceGame.*;

public class LevelScreen extends BaseScreen {
    private Table table;
    private Skin skin;
    private Spaceship spaceship;

    //BUTTON
    private Texture buttonTexture;
    private TextureRegion buttonTextureRegion;
    private TextureRegionDrawable myTexRegionDrawable;
    private Button soundButton;
    private boolean soundMuted;

    private Map<Integer, SpaceshipEnemy> enemySpaceships = new HashMap<>();

    //CHAT
    private TextField chatText;
    private final List<MessageEntity> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private final List<String> messagesToSend = Collections.synchronizedList(new ArrayList<>());
    private List<Label> chatMessagesLabels = new ArrayList<>();

    private BaseActor space;
    private BaseActor winMessage;

    private Random random = new Random();

    @Override
    public void initialize() {
        space = new BaseActor(0, 0, mainStage);
        space.loadTexture("assets/space.png");
        space.setWidth(GAME_WIDTH - CHAT_WIDTH);
        space.setHeight(GAME_HEIGHT);

        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        table = new Table(skin);

        chatText = new TextField("", skin);
        chatText.setPosition(GAME_WIDTH - CHAT_WIDTH, 0);
        chatText.setSize(250, 50);
        chatText.setTextFieldListener((textField, c) -> {
            if ((int) c == 13) {
                messagesToSend.add(textField.getText());
                textField.setText("");
            }
        });

        createSoundButton("assets/sound4.png");

        mainStage.addActor(chatText);

    }

    private void createSoundButton(String path) {
        buttonTexture = new Texture(Gdx.files.internal(path));
        buttonTextureRegion = new TextureRegion(buttonTexture);
        myTexRegionDrawable = new TextureRegionDrawable(buttonTextureRegion);
        soundButton = new ImageButton(myTexRegionDrawable);
        soundButton.setPosition(5, 850);
        soundButton.setSize(40, 40);

        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Sound changed! " + GlobalConfig.isMuted);
                if (GlobalConfig.isMuted) {
                    createSoundButton("assets/sound4.png");
                } else {
                    createSoundButton("assets/sound-muted.png");
                }
                GlobalConfig.isMuted = !GlobalConfig.isMuted;
            }
        });

        mainStage.addActor(soundButton);
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

    public void addWinMessage() {
        winMessage = new BaseActor(0, 0, mainStage);
        winMessage.loadTexture("assets/message-win.png");
        winMessage.centerAtPosition(GAME_WIDTH / 2, GAME_HEIGHT / 2);
        winMessage.setOpacity(0);
        winMessage.addAction(Actions.delay(1));
        winMessage.addAction(Actions.after(Actions.fadeIn(1)));
    }

    private void drawLines() {
        for (int i = 0; i < 900; i += 25) {
            ShapeRenderer shapeRenderer = new ShapeRenderer();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 0, 1, 1);
            shapeRenderer.line(GAME_WIDTH - CHAT_WIDTH, i, GAME_WIDTH, i);
            shapeRenderer.end();
        }
    }

    private void updateChat() {
        float chatDisplayPosition = 50;

        for (Label chatLabel : chatMessagesLabels) {
            chatLabel.remove();
        }
        chatMessagesLabels.clear();

        synchronized (chatMessages) { //only needed in netty
            int msgCount = chatMessages.size();
            for (int i = msgCount; i > 0; i--) {
                if (chatDisplayPosition >= 850f) {
                    break;
                }

                MessageEntity message = chatMessages.get(i - 1);
                Label text = new Label(message.getPlayerName() + " " + message.getContent(), skin);
                text.setWrap(true);
                text.setWidth(198);
                float lines = (text.getPrefHeight() - 25.0f) / 20.0f;
                float height = 25f + lines * 25f;
                text.setHeight(height);
                float[] rgb = message.getRgb();
                text.setColor(new Color(rgb[0], rgb[1], rgb[2], 1f));

                text.setPosition(GAME_WIDTH - CHAT_WIDTH + 2, chatDisplayPosition);
                chatDisplayPosition += text.getHeight();

                chatMessagesLabels.add(text);
                uiStage.addActor(text);
            }
        }
    }

    @Override
    public void update(float delta) {
        updateChat();
        drawLines();

        int result = random.nextInt(100);
        if (result <= 1) {
            messagesToSend.add("Test message");
        }
    }

    public List<MessageEntity> getChatMessages() {
        return chatMessages;
    }

    public List<String> getMessagesToSend() {
        return messagesToSend;
    }

    public boolean isSoundMuted() {
        return soundMuted;
    }
}
