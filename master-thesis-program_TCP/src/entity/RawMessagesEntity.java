package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RawMessagesEntity implements Serializable {
    private List<String> rawMessages = new ArrayList<>();

    public List<String> getRawMessages() {
        return rawMessages;
    }

    public void setRawMessages(List<String> rawMessages) {
        this.rawMessages = rawMessages;
    }
}
