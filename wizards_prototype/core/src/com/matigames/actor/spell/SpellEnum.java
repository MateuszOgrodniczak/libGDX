package com.matigames.actor.spell;

public enum SpellEnum {
    FIREBOLT("Firebolt"), FIREBALL("Fireball"), FIREBOULDER("Fireboulder"), ICE_SPIKE("Ice spike"),
    ICE_CROWN("Ice crown"), ENERGY_WAVE("Energy wave"), LIGHTNING("Lightning"), MAGIC_BARRIER("Magic barrier"),
    FROST_NOVA("Frost nova"), BLINK("Blink"), TORNADO("Tornado");

    private String spellName;

    SpellEnum(String spellName) {
        this.spellName = spellName;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }

    public static SpellEnum fromString(String name) {
        for (SpellEnum spellEnum : SpellEnum.values()) {
            if (spellEnum.name().replace("_", " ").equals(name.toUpperCase())) {
                return spellEnum;
            }
        }
        return null;
    }
}
