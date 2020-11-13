package com.matigames.actor.spell;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.matigames.actor.Player;

public class MagicBurst extends Spell {
    private SpellDirection direction;
    private Player player;

    private float xDisplacement;
    private float yDisplacement;

    public MagicBurst(Stage s, SpellSchool school, SpellDirection direction, Player player) {
        super(0, 0, s);

        this.direction = direction;
        this.player = player;
        adjustPosition();
        setBoundaryPolygon(4, 1, 1);

        if(school == SpellSchool.FIRE) {
            loadAnimationFromSheet("assets/test-spells/FireBurst_64x64.png", 1, 29, 0.05f, true);
            yDisplacement = - 75;
        }
        else if (school == SpellSchool.ICE) {
            String[] animations = new String[11];
            for(int i = 1; i<12; i++) {
                String frameNumber = i <= 9 ? ("0" + i) : ""+i;
                animations[i-1] = "assets/ice/weak/weak_ice00" + frameNumber + ".png";
            }
            loadAnimationFromFiles(animations, 0.05f, true);
            yDisplacement = -100;
        } else if (school == SpellSchool.ARCANE) {
            loadAnimationFromSheet("assets/test-spells/MagicBarrier_64x64.png", 1, 33, 0.05f, true);
            yDisplacement = -100;
        } else if (school == SpellSchool.STORM) {
        }

        setWidth(player.getWidth());
        setHeight(getWidth());
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        adjustPosition();
    }

    private void adjustPosition() {
        float x = direction == SpellDirection.LEFT ? player.getX()-75 : player.getX()+player.getWidth()-75;
        float y = player.getY() + player.getHeight() + yDisplacement;
        setPosition(x, y);
    }
}
