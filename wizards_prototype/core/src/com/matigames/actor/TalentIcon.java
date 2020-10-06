package com.matigames.actor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.matigames.character.CharacterDetails;
import com.matigames.util.IconListener;

public class TalentIcon extends BaseActor {
    private CharacterDetails characterDetails;

    private String name;
    private boolean passive;
    private String hint;
    private int level;
    private int maxLevel;
    private int requiredPoints;
    private boolean hasConnection;
    private TextTooltip tooltip;
    private Label label;
    private String[] talentDescriptions;
    private TalentIcon prerequisiteTalent;

    public TalentIcon(float x, float y, Stage s, String name, boolean passive, int maxLevel, String texture, Skin skin, String[] description, Integer skillLevel, CharacterDetails characterDetails, boolean hasConnection) {
        super(x, y, s);

        this.name = name;
        this.passive = passive;
        this.maxLevel = maxLevel;
        this.hint = description[0];
        this.requiredPoints = skillLevel * 5;
        this.characterDetails = characterDetails;
        this.hasConnection = hasConnection;
        this.talentDescriptions = description;
        this.label = new Label(name + "\n" + level + "/" + maxLevel, skin);
        loadTexture(texture);

        this.addListener(new IconListener(this));

        tooltip = new TextTooltip(hint, skin);
        tooltip.setInstant(true);

        this.addListener(tooltip);
    }

    public void updateLabel() {
        this.label.setText(name + "\n" + level + "/" + maxLevel);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getRequiredPoints() {
        return requiredPoints;
    }

    public void setRequiredPoints(int requiredPoints) {
        this.requiredPoints = requiredPoints;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public CharacterDetails getCharacterDetails() {
        return characterDetails;
    }

    public void setCharacterDetails(CharacterDetails characterDetails) {
        this.characterDetails = characterDetails;
    }

    public boolean isHasConnection() {
        return hasConnection;
    }

    public void setHasConnection(boolean hasConnection) {
        this.hasConnection = hasConnection;
    }

    public TextTooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(TextTooltip tooltip) {
        this.tooltip = tooltip;
    }

    public String[] getTalentDescriptions() {
        return talentDescriptions;
    }

    public void setTalentDescriptions(String[] talentDescriptions) {
        this.talentDescriptions = talentDescriptions;
    }

    public TalentIcon getPrerequisiteTalent() {
        return prerequisiteTalent;
    }

    public void setPrerequisiteTalent(TalentIcon prerequisiteTalent) {
        this.prerequisiteTalent = prerequisiteTalent;
    }

    public boolean isPassive() {
        return passive;
    }
}
