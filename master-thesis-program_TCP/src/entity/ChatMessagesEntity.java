package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatMessagesEntity implements Serializable {
    private List<MessageEntity> messageEntities = new ArrayList<>();

    public List<MessageEntity> getMessageEntities() {
        return messageEntities;
    }

    public void setMessageEntities(List<MessageEntity> messageEntities) {
        this.messageEntities = messageEntities;
    }
}
