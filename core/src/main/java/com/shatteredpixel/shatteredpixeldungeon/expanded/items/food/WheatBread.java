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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class WheatBread extends Food {

	{
		stackable = true;
		image = ItemSpriteSheet.WHEAT_BREAD;

		defaultAction = AC_EAT;

		bones = false;
	}

    @Override
	protected void satisfy( Hero hero ){
        super.satisfy(hero);

        if (WoodenSpoon.foodEffectAmplifier() != -1) {
            Buff.affect(Dungeon.hero, CarbohydrateRush.class).set(1 + WoodenSpoon.foodEffectAmplifier());
        }
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

    public static class CarbohydrateRush extends Buff {

        public int left;

        {
            type = buffType.POSITIVE;

            announced = false;
            mnemonicExtended = false;
        }

        public int absorb( int damage ) {
            left--;
            if (left <= 0) detach();
            return Math.max(damage - 1, 0);
        }

        public void mnemonicBoost( int value ) {
            left += value;
        }

        public void set(int hits){
            if (left > 0) left += hits;
            else left = hits;
        }

        @Override
        public int icon() {
            return BuffIndicator.WHEAT_CARBOHYDRATE_RUSH;
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", left);
        }

        private static final String LEFT = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( LEFT, left );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getInt( LEFT );
        }

    }
}
