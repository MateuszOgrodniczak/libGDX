package entity;

import java.io.Serializable;

public class MessageEntity implements Serializable {
    private String playerName;
    private String content;
    private float[] rgb;

    public MessageEntity() {
    }

    public MessageEntity(String playerName, String content, float[] rgb) {
        this.playerName = playerName;
        this.content = content;
        this.rgb = rgb;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getRgb() {
        return rgb;
    }

    public void setRgb(float[] rgb) {
        this.rgb = rgb;
    }
}
