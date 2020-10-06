package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OtherPlayersEntity implements Serializable {
    private List<PlayerEntity> players = new ArrayList<>();

    public List<PlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerEntity> players) {
        this.players = players;
    }
}
