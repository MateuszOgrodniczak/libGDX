package com.matigames.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.matigames.actor.BaseActor;
import com.matigames.actor.rune.BaseRune;
import com.matigames.actor.spell.SpellEnum;
import com.matigames.character.CharacterDetails;
import com.matigames.game.WizardsGame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.matigames.config.GlobalConfig.GAME_HEIGHT;
import static com.matigames.config.GlobalConfig.GAME_WIDTH;

public class SpellbookScreen extends BaseScreen {
    private int currentPage;
    private int lastPage;
    //4 spells per 2 pages
    private Map<Integer, BaseRune[]> pagesToRunes;

    public SpellbookScreen(Game game) {
        super(game);
    }

    private void changeVisibility() {
        for (Integer key : pagesToRunes.keySet()) {
            BaseRune[] runes = pagesToRunes.get(key);
            for (BaseRune rune : runes) {
                if (rune != null) {
                    rune.setVisible(key == currentPage);
                }
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        currentPage = 1;
        lastPage = currentPage;

        BaseActor spellbook = new BaseActor(GAME_WIDTH / 2 - 500, GAME_HEIGHT / 2 - 400, uiStage);
        spellbook.loadTexture("assets/spellbook.jpg");
        spellbook.setWidth(GAME_WIDTH * 0.8f);
        spellbook.setHeight(GAME_HEIGHT * 0.8f);

        TextButton leftArrow = new TextButton("Previous page", skin);
        TextButton rightArrow = new TextButton("Next page", skin);
        leftArrow.setPosition(spellbook.getX(), spellbook.getY());
        rightArrow.setPosition(spellbook.getX() + spellbook.getWidth() - rightArrow.getWidth(), spellbook.getY());
        leftArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentPage > 1) {
                    currentPage--;
                    changeVisibility();
                }
            }
        });
        rightArrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentPage < lastPage) {
                    currentPage++;
                    changeVisibility();
                }
            }
        });
        uiStage.addActor(leftArrow);
        uiStage.addActor(rightArrow);

        float x = spellbook.getX() + spellbook.getWidth() / 2;
        float y = spellbook.getY() + spellbook.getHeight() / 2;
        float[][] positions = new float[][]{
                new float[]{x - spellbook.getWidth() / 6, y + spellbook.getHeight() / 6},
                new float[]{x - spellbook.getWidth() / 6, y - spellbook.getHeight() / 6},
                new float[]{x + spellbook.getWidth() / 6, y + spellbook.getHeight() / 6},
                new float[]{x + spellbook.getWidth() / 6, y - spellbook.getHeight() / 6},
        };

        CharacterDetails details = ((WizardsGame) game).getCharacterDetails();
        Set<SpellEnum> spellEnums = details.getSpells();
        pagesToRunes = new HashMap<>();
        int i = 0;
        for (SpellEnum spellEnum : spellEnums) {
            String spell = spellEnum.name();
            spell = spell.replace("_", "").replace(" ", "").toLowerCase();
            String spellPath = "assets/runes/" + spell + "1.png";
            BaseRune rune;
            try {
                rune = new BaseRune(0, 0, uiStage, new String[]{spellPath}, spell, skin);
            } catch (GdxRuntimeException e) {
                continue;
            }
            if (i >= positions.length) {
                lastPage++;
                i = 0;
            }

            float currentX = positions[i][0];
            float currentY = positions[i][1];

            rune.setSize(250, 250);
            rune.centerAtPosition(currentX, currentY);
            if (lastPage != currentPage) {
                rune.setVisible(false);
            }

            BaseRune[] runes = pagesToRunes.get(lastPage);
            if (runes == null) {
                runes = new BaseRune[4];
            }
            runes[i] = rune;
            pagesToRunes.put(lastPage, runes);
            i++;
        }
    }

    @Override
    public void update(float dt) {

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
}
