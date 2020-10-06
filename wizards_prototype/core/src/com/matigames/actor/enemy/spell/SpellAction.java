package com.matigames.actor.enemy.spell;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.matigames.actor.enemy.BaseEnemy;
import com.matigames.actor.enemy.Imp;
import com.matigames.actor.enemy.Mage;

public class SpellAction extends Action {
    private BaseEnemy enemy;
    private EnemySpellEnum spellType;

    public SpellAction(BaseEnemy enemy, EnemySpellEnum spellType) {
        this.enemy = enemy;
        this.spellType = spellType;
    }

    @Override
    public boolean act(float delta) {
        EnemySpell spell = null;
        if (spellType == EnemySpellEnum.POOR_MAGICBOLT) {
            spell = new PoorMagicbolt(0, 0, enemy.getStage());
        } else if (spellType == EnemySpellEnum.FROSTBOLT) {
            spell = new Frostbolt(0, 0, enemy.getStage());
        } else if (spellType == EnemySpellEnum.FIRE_MISSILE) {
            spell = new FireMissile(0, 0, enemy.getStage());
        } else if (spellType == EnemySpellEnum.MASSIVE_FIRE_MISSILE) {
            spell = new MassiveFireMissile(0, 0, enemy.getStage());
        } else if (spellType == EnemySpellEnum.SUMMON_IMP) {
            Imp imp = new Imp(0, 0, (Mage) enemy);
            int summonedImps = ((Mage) enemy).getSummonedImps();
            imp.setY(enemy.getY());
            if (summonedImps % 2 != 0) {
                imp.setX(enemy.getX() + enemy.getWidth() + summonedImps * 25);
            } else {
                imp.setX(enemy.getX() - summonedImps * 25);
            }
        }
        if (spell != null) {
            spell.centerAtActor(enemy);
        }

        if (enemy.getAnimation() == null || enemy.getAnimation() != enemy.getDeathAnimation()) {
            enemy.setAnimation(enemy.getIdleAnimation());
        }
        enemy.clearActions();
        return false;
    }
}
