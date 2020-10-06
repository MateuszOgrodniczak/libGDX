package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.matigames.actor.Arrow;
import com.matigames.actor.TalentIcon;
import com.matigames.actor.spell.SpellSchool;
import com.matigames.character.TalentDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class CharacterScreen extends BaseScreen {

    //talents
    private Label pointsLeftLabel;
    private TalentIcon[][] currentTalents;
    private TalentIcon[][] fireTalents;
    private TalentIcon[][] frostTalents;
    private TalentIcon[][] stormTalents;
    private TalentIcon[][] arcaneTalents;

    //trees
    private Table currentTree;
    private Table fireTree;
    private Table frostTree;
    private Table stormTree;
    private Table arcaneTree;

    //background
    private NinePatchDrawable background;

    public CharacterScreen(Game game) {
        super(game);
    }

    @Override
    public void initialize() {
        super.initialize();

        NinePatch patch = new NinePatch(new Texture(Gdx.files.internal("assets/icons/white-background.jpg")),
                3, 3, 3, 3);
        background = new NinePatchDrawable(patch);

        pointsLeftLabel = new Label("Talent points remaining: 0", skin);

        initFireTalents(skin);
        initFrostTalents(skin);

        initTree(SpellSchool.FIRE);
        initTree(SpellSchool.ICE);

        currentTree = fireTree;
        currentTalents = fireTalents;
        currentTree.setVisible(true);

        Table treesSelectionTable = new Table();
        treesSelectionTable.setX(GAME_WIDTH * 0.3f);
        treesSelectionTable.setY(GAME_HEIGHT * 0.9f);

        Button pyromancerTreeSwitch = new TextButton("Pyromancer", skin);
        pyromancerTreeSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentTree.setVisible(false);
                currentTree = fireTree;
                currentTalents = fireTalents;
                currentTree.setVisible(true);
            }
        });
        Button iceMageTreeSwitch = new TextButton("Ice mage", skin);
        iceMageTreeSwitch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentTree.setVisible(false);
                currentTree = frostTree;
                currentTalents = frostTalents;
                currentTree.setVisible(true);
            }
        });
        Button stormlordTreeSwitch = new TextButton("Storm mage", skin);
        Button sorcererTreeSwitch = new TextButton("Sorcerer", skin);

        treesSelectionTable.add(pyromancerTreeSwitch).width(100).padRight(10);
        treesSelectionTable.add(iceMageTreeSwitch).width(100).padRight(10);
        treesSelectionTable.add(stormlordTreeSwitch).width(100).padRight(10);
        treesSelectionTable.add(sorcererTreeSwitch).width(100);

        treesSelectionTable.row();
        treesSelectionTable.add(pointsLeftLabel).width(100).padTop(25);
        uiStage.addActor(treesSelectionTable);

        /*TextButton exitButton = new TextButton("Back to game menu", skin);
        exitButton.setPosition(5, GAME_HEIGHT * 0.9f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        uiStage.addActor(exitButton);*/
        //mainStage.addActor(exitButton);
    }

    @Override
    public void show() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(this);
        im.addProcessor(uiStage);
    }

    @Override
    public void hide() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        im.removeProcessor(uiStage);
    }

    private void initTree(SpellSchool school) {
        Table table = new Table();
        table.setBackground(background);
        table.debugTable();
        table.setVisible(false);
        table.setX(GAME_WIDTH * 0.25f);
        table.setY(GAME_HEIGHT * 0.5f);

        switch (school) {
            case FIRE:
                currentTalents = fireTalents;
                fireTree = table;
                break;
            case ICE:
                currentTalents = frostTalents;
                frostTree = table;
                break;
            case STORM:
                currentTalents = stormTalents;
                stormTree = table;
                break;
        }

        createTalentTree(table);
        uiStage.addActor(table);
    }

    private void initFrostTalents(Skin skin) {
        frostTalents = new TalentIcon[][]{
                new TalentIcon[]{
                        new TalentIcon(0, 0, uiStage, "Ice crown", false, 1, "assets/icons/crystal-3.png", skin,
                                TalentDescription.FIRE_FIREBOULDER, 0, characterDetails, false)
                }
        };
        setSpentPoints(frostTalents);
    }

    private void setSpentPoints(TalentIcon[][] talents) {
        Map<String, Integer> points = characterDetails.getTalentToLevel();
        for (TalentIcon[] talentsRow : talents) {
            for (TalentIcon talent : talentsRow) {
                Integer level = points.get(talent.getName());
                if (level != null) {
                    talent.setLevel(level);
                    talent.updateLabel();
                }
            }
        }
    }

    private void initFireTalents(Skin skin) {
        TalentIcon firebomb = new TalentIcon(10, GAME_HEIGHT - 25, uiStage, "Firebomb", true, 5, "assets/icons/fire_generic-icon.png", skin,
                TalentDescription.FIRE_FIREBOMB, 0, characterDetails, false);
        TalentIcon ignite = new TalentIcon(20 + 50, GAME_HEIGHT - 25, uiStage, "Ignite", true, 3, "assets/icons/fire_generic-icon.png", skin,
                TalentDescription.FIRE_IGNITE, 0, characterDetails, true);

        TalentIcon impFirebolt = new TalentIcon(10, GAME_HEIGHT - 35 - 50, uiStage, "Improved\nfirebolt", true, 5, "assets/icons/firebolt-icon.png", skin,
                TalentDescription.FIRE_IMP_FIREBOLT, 1, characterDetails, true);
        TalentIcon spreading = new TalentIcon(0, 0, uiStage, "Spreading\nflames", true, 1, "assets/icons/fire_generic-icon.png", skin,
                TalentDescription.FIRE_SPREADING, 1, characterDetails, true);
        spreading.setPrerequisiteTalent(ignite);
        TalentIcon strongFlame = new TalentIcon(0, 0, uiStage, "Strong\nflame", true, 3, "assets/icons/fire_generic-icon.png", skin,
                TalentDescription.FIRE_STRONG, 1, characterDetails, false);

        TalentIcon fireball = new TalentIcon(10, GAME_HEIGHT - 35 - 50, uiStage, "Fireball", false, 1, "assets/icons/fireball-icon.png", skin,
                TalentDescription.FIRE_FIREBALL, 2, characterDetails, true);
        fireball.setPrerequisiteTalent(impFirebolt);
        TalentIcon impSpreading = new TalentIcon(0, 0, uiStage, "Imp. spreading\nflames", true, 2, "assets/icons/fire_generic-icon.png", skin,
                TalentDescription.FIRE_IMP_SPREADING, 2, characterDetails, false);
        impSpreading.setPrerequisiteTalent(spreading);

        TalentIcon fireboulder = new TalentIcon(10, GAME_HEIGHT - 35 - 50, uiStage, "Fireboulder", false, 1, "assets/icons/fireboulder-icon.png", skin,
                TalentDescription.FIRE_FIREBOULDER, 3, characterDetails, false);
        fireboulder.setPrerequisiteTalent(fireball);


        fireTalents = new TalentIcon[][]{
                new TalentIcon[]{
                        firebomb, ignite
                },
                new TalentIcon[]{
                        impFirebolt, spreading, strongFlame
                },
                new TalentIcon[]{
                        fireball, impSpreading
                },
                new TalentIcon[]{
                        fireboulder
                }
        };

        setSpentPoints(fireTalents);
    }

    private void createTalentTree(Table table) {
        List<Label> labels = new ArrayList<>();
        List<Arrow> connections = new ArrayList<>();
        for (TalentIcon[] talent : currentTalents) {
            for (TalentIcon icon : talent) {
                labels.add(icon.getLabel());
                if (icon.isHasConnection()) {
                    connections.add(new Arrow(0, 0, uiStage, "assets/icons/arrow1.png"));
                } else {
                    connections.add(null);
                }
                table.add(icon).height(50).width(50);
            }
            table.row();
            for (Label label : labels) {
                table.add(label);
            }
            table.row();
            for (Arrow arrow : connections) {
                if (arrow == null) {
                    table.add();
                } else {
                    table.add(arrow).height(50).width(50);
                }
            }
            table.row();
            connections.clear();
            labels.clear();
        }
    }

    @Override
    public void update(float dt) {
        pointsLeftLabel.setText("Talent points remaining: " + (characterDetails.getLevel() - characterDetails.getSpentTalentPoints()));
        for (TalentIcon[] talentsRow : currentTalents) {
            for (TalentIcon talent : talentsRow) {
                Label label = talent.getLabel();
                int level = characterDetails.getLevel();
                int spentPoints = characterDetails.getSpentTalentPoints();
                if (level - spentPoints > 0) {
                    label.setColor(1, 1, 1, 1);
                } else {
                    label.setColor(0.5f, 0.5f, 0.5f, 1);
                }
                TalentIcon prerequisite = talent.getPrerequisiteTalent();
                if (spentPoints < talent.getRequiredPoints() || (prerequisite != null && prerequisite.getLevel() != prerequisite.getMaxLevel())) {
                    label.setColor(1, 0, 0, 1);
                    talent.getTooltip().getActor().setColor(1, 0, 0, 1);
                } else {
                    talent.getTooltip().getActor().setColor(1, 1, 1, 1);
                }
                if (talent.getLevel() > 0) {
                    if (talent.getLevel() == talent.getMaxLevel()) {
                        label.setColor(0, 1, 0, 1);
                    } else {
                        label.setColor(1, 1, 0, 1);
                    }
                }
            }
        }
    }
}
