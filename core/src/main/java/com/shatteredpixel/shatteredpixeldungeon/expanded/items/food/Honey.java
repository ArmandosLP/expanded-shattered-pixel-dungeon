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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Honey extends SmallRation {

	{
		image = ItemSpriteSheet.HONEY;
		energy = Hunger.HUNGRY/2f;
	}

    @Override
    protected void satisfy(Hero hero) {
        super.satisfy(hero);

        if (WoodenSpoon.foodEffectAmplifier() == -1) return;

        // heal 2/3/4/5
        int heal = 1 + WoodenSpoon.foodEffectAmplifier();
        hero.HP = Math.min(hero.HP + heal, hero.HT);
        hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
    }


    @Override
    public String desc() {
        String desc = super.desc();
        desc += "\n\n" + Messages.get(WoodenSpoon.class,"function",Messages.get(getClass().getSuperclass(),"name"));

        if (WoodenSpoon.foodEffectAmplifier() != -1){
            desc += "\n\n" + Messages.get(WoodenSpoon.class,"react") + "\n";
            desc += "\n" + Messages.get(this,"spoon_buff", 1 + WoodenSpoon.foodEffectAmplifier());
        }

        return desc;
    }

}
