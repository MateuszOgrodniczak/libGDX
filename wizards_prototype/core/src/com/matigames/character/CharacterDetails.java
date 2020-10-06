package com.matigames.character;

import com.matigames.actor.spell.SpellEnum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CharacterDetails {
    private String nickname;
    private int level;
    private int spentTalentPoints;
    private Map<String, Integer> talentToLevel = new HashMap<>();
    private Set<SpellEnum> spells = new HashSet<>();

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSpentTalentPoints() {
        return spentTalentPoints;
    }

    public void setSpentTalentPoints(int spentTalentPoints) {
        this.spentTalentPoints = spentTalentPoints;
    }

    public Map<String, Integer> getTalentToLevel() {
        return talentToLevel;
    }

    public void setTalentToLevel(Map<String, Integer> talentToLevel) {
        this.talentToLevel = talentToLevel;
    }

    public Set<SpellEnum> getSpells() {
        return spells;
    }

    public void setSpells(Set<SpellEnum> spells) {
        this.spells = spells;
    }
}
