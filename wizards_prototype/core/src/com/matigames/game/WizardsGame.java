package com.matigames.game;

import com.matigames.actor.spell.SpellEnum;
import com.matigames.character.CharacterDetails;
import com.matigames.screen.MenuScreen;

import java.util.Set;
import java.util.TreeSet;

public class WizardsGame extends BaseGame {
    private CharacterDetails characterDetails;

    @Override
    public void create() {
        super.create();

        characterDetails = new CharacterDetails();
        characterDetails.setNickname("Mati");
        characterDetails.setLevel(20);
        Set<SpellEnum> basicSpells = new TreeSet<>();
        basicSpells.add(SpellEnum.FIREBOLT);
        basicSpells.add(SpellEnum.ICE_SPIKE);
        basicSpells.add(SpellEnum.FROST_NOVA);
        basicSpells.add(SpellEnum.BLINK);
       /* basicSpells.add("icecrown");
        basicSpells.add("frostnova");
        basicSpells.add("fireball");
        basicSpells.add("fireboulder");*/
        // basicSpells.add(SpellEnum.MAGIC_BARRIER.name());
        characterDetails.setSpells(basicSpells);

        this.setScreen(new MenuScreen(this));
    }

    @Override
    public void update(float delta) {
        getScreen().render(delta);
    }

    public CharacterDetails getCharacterDetails() {
        return characterDetails;
    }
}
