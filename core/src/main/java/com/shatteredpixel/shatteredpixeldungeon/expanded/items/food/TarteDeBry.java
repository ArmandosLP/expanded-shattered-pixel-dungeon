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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.FoodHtBoost;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TarteDeBry extends MeatPie {
	
	{
		image = ItemSpriteSheet.BRYMLENT_TART;
		energy = Hunger.STARVING*2f;
	}
	
	@Override
	protected void satisfy(Hero hero) {
		super.satisfy( hero );


        if (WoodenSpoon.foodEffectAmplifier() != -1) {
            hero.buff(WellFed.class).specialBuff(this,1 + WoodenSpoon.foodEffectAmplifier());
            Buff.affect(hero, FoodHtBoost.class).addLevel(WoodenSpoon.foodEffectAmplifier()); // +1/2/3/4 HT
            hero.updateHT( true );
        }
	}



    @Override
    public String desc() {
        String desc = super.desc();
        desc += "\n\n" + Messages.get(WoodenSpoon.class,"function",Messages.get(getClass().getSuperclass(),"name"));

        if (Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.CLERIC){
            desc += "\n\n" + Messages.get(this,"cleric_special_buff");
        }

        if (WoodenSpoon.foodEffectAmplifier() != -1){
            desc += "\n\n" + Messages.get(WoodenSpoon.class,"react") + "\n";
            desc += "\n" + Messages.get(this,"spoon_buff", 1 + WoodenSpoon.foodEffectAmplifier(),WoodenSpoon.foodEffectAmplifier());
        }

        return desc;
    }
	
//	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {
//
//		@Override
//		public boolean testIngredients(ArrayList<Item> ingredients) {
//			boolean pasty = false;
//			boolean ration = false;
//			boolean meat = false;
//
//			for (Item ingredient : ingredients){
//				if (ingredient.quantity() > 0) {
//					if (ingredient instanceof Pasty || ingredient instanceof PhantomMeat) {
//						pasty = true;
//					} else if (ingredient.getClass() == Food.class) {
//						ration = true;
//					} else if (ingredient instanceof MysteryMeat
//							|| ingredient instanceof StewedMeat
//							|| ingredient instanceof ChargrilledMeat
//							|| ingredient instanceof FrozenCarpaccio) {
//						meat = true;
//					}
//				}
//			}
//
//			return pasty && ration && meat;
//		}
//
//		@Override
//		public int cost(ArrayList<Item> ingredients) {
//			return 6;
//		}
//
//		@Override
//		public Item brew(ArrayList<Item> ingredients) {
//			if (!testIngredients(ingredients)) return null;
//
//			for (Item ingredient : ingredients){
//				ingredient.quantity(ingredient.quantity() - 1);
//			}
//
//			return sampleOutput(null);
//		}
//
//		@Override
//		public Item sampleOutput(ArrayList<Item> ingredients) {
//			return new HuntersSandwich();
//		}
//	}
}
