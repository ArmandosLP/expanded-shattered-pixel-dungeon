/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class StrongStatue extends Statue {

	{
		spriteClass = StatueSprite.class;

		EXP = 0;
		state = PASSIVE;

		properties.add(Property.INORGANIC);

        HUNTING = new Hunting();
	}

    protected Potion potion = null;

    @Override
    protected boolean act() {
        if (buff(StatueMindVision.class) != null &&
                Dungeon.hero != null &&
                !isCharmedBy(Dungeon.hero) &&
                state != HUNTING
        ){
            beckon(Dungeon.hero.pos);
        }
        return super.act();
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );

        int ofs;
        do {
            ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
        } while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
        Dungeon.level.drop( potion, pos + ofs ).sprite.drop( pos );

        potion = null;
    }

    public class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;

            if (paralysed <= 0 && enemy != null && !isCharmedBy(enemy) && potion != null){

                boolean show = sprite != null && sprite.visible;

                if (show) { GLog.w( Messages.get(StrongStatue.this,"potion", name(), potion.name()) ); }

                if (potion instanceof PotionOfMindVision){
                    Buff.prolong(StrongStatue.this, StatueMindVision.class, StatueMindVision.DURATION );
                    if (show) SpellSprite.show(StrongStatue.this, SpellSprite.VISION, 1, 0.77f, 0.9f);
                } else if (potion instanceof PotionOfHaste) {
                    Buff.prolong(StrongStatue.this, Haste.class, Haste.DURATION);
                    if (show) SpellSprite.show(StrongStatue.this, SpellSprite.HASTE, 1, 1, 0);
                } else if (potion instanceof PotionOfHealing){
                    PotionOfHealing.cure(StrongStatue.this);
                    PotionOfHealing.heal(StrongStatue.this);
                    if (show) SpellSprite.show(StrongStatue.this, SpellSprite.HEALING, 1, 1, 0);
                }

                potion = null;

                spend(TICK);
                return true;
            }

            return super.act(enemyInFOV, justAlerted);
        }
    }

    public StrongStatue() {
		super();

        // Using Dungeon.depth keeps seed consistency if talented mobs challenge is off
        Random.pushGenerator(Dungeon.depth);
        switch (Random.Int(3)){
            case 0:
                potion = new PotionOfMindVision();
                break;
            case 1:
                potion = new PotionOfHealing();
                break;
            case 2:
                potion = new PotionOfHaste();
                break;
        }
        Random.popGenerator();
	}

	private static final String POTION	= "potion";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
        bundle.put( POTION, potion );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		potion = (Potion) bundle.get( POTION );
	}

	@Override
	public String description() {
        String desc = super.description();
        if (potion != null) desc += "\n\n" + Messages.get(this, "talent", potion.name());
        return desc;

	}


    public static class StatueMindVision extends FlavourBuff{
        // Fake mind vision class, just visual for the player
        // Avoid using mind vision which only hero can get

        public static final float DURATION = 20f;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.MIND_VISION;
        }

        @Override
        public String name() {
            return Messages.get(MindVision.class,"name");
        }

        @Override
        public String desc() {
            return Messages.get(MindVision.class, "desc", dispTurns());
        }

    }
}
