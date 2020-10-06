package com.matigames.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matigames.actor.TalentIcon;
import com.matigames.actor.spell.SpellEnum;
import com.matigames.character.CharacterDetails;

public class IconListener extends ClickListener {
    private TalentIcon icon;

    public IconListener(TalentIcon icon) {
        this.icon = icon;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        System.out.println("HOVER");
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        System.out.println("HOVER EXIT");
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        int level = icon.getLevel();
        String iconName = icon.getName();
        CharacterDetails details = icon.getCharacterDetails();
        int charLevel = details.getLevel();
        int spentPoints = details.getSpentTalentPoints();
        if (charLevel - spentPoints > 0 &&
                level < icon.getMaxLevel() &&
                spentPoints >= icon.getRequiredPoints()) {
            TalentIcon prerequisite = icon.getPrerequisiteTalent();
            if (prerequisite != null && prerequisite.getLevel() != prerequisite.getMaxLevel()) {
                return;
            }
            details.getTalentToLevel().put(iconName, level + 1);
            if (!icon.isPassive()) {
                details.getSpells().add(SpellEnum.fromString(iconName));
            }
            icon.setLevel(level + 1);
            icon.getLabel().setText(iconName + "\n" + icon.getLevel() + "/" + icon.getMaxLevel());
            details.setSpentTalentPoints(spentPoints + 1);

            icon.setHint(icon.getTalentDescriptions()[icon.getLevel()]);
            icon.getTooltip().getActor().setText(icon.getHint());
        }
    }
}
