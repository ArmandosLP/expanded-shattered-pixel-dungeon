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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.Frenzy;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Pasta extends Pasty {

	{
        image = ItemSpriteSheet.PASTA;
        energy = Hunger.STARVING;

        bones = false;
	}

    // No special foods for Holiday :(
	@Override
	public void reset() {
	}

	@Override
	protected void eatSFX() {
		super.eatSFX();
	}

    // Special effects for holidays work but the sprite don't change
	@Override
	protected void satisfy(Hero hero) {
		super.satisfy(hero);

        if (WoodenSpoon.foodEffectAmplifier() == -1) return;
        Buff.affect(hero, Frenzy.class).set(1, 0.25f * (float) WoodenSpoon.foodEffectAmplifier());
	}

	@Override
	public String name() {
        return Messages.get(this, "name");
	}

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc"); // Ignoring holiday desc
        desc += "\n\n" + Messages.get(WoodenSpoon.class,"function",Messages.get(getClass().getSuperclass(),"name"));

        if (WoodenSpoon.foodEffectAmplifier() != -1){
            desc += "\n\n" + Messages.get(WoodenSpoon.class,"react") + "\n";
            desc += "\n" + Messages.get(this, "spoon_buff",
                    Messages.decimalFormat("#.##", 100 * (0.25f * (float) WoodenSpoon.foodEffectAmplifier())));
        }

        return desc;
    }

}
