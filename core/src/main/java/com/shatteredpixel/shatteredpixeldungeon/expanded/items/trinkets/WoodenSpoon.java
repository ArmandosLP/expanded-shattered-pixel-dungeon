
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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.Bun;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.DriedMeat;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.GoldenBerry;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.Gruel;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.Honey;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.HuntersSandwich;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.Ribs;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.RyeBread;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.SurvivalRation;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.TarteDeBry;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.WheatBread;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.Pasta;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Berry;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SupplyRation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.LinkedHashMap;
import java.util.Map;

public class WoodenSpoon extends Trinket {

    {
        // Credits for the sprite: Alan "El Buho"
        // Including all the foods from the spoon
        image = ItemSpriteSheet.WOODEN_SPOON;
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            switch (foodEffectAmplifier(buffedLvl())){
                case 1:  return Messages.get(this, "stats_desc", Messages.get(this,"level_tiny"));
                case 2:  return Messages.get(this, "stats_desc", Messages.get(this,"level_modest"));
                case 3:  return Messages.get(this, "stats_desc", Messages.get(this,"level_large"));
                case 4:  return Messages.get(this, "stats_desc", Messages.get(this,"level_huge"));
                default: return Messages.get(this, "typical_stats_desc");
            }
        } else {
            return Messages.get(this, "typical_stats_desc");
        }
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 8(14) -> 10(24) -> 12(36)
        return 6+2*level();
    }

    public static int foodEffectAmplifier(){
        return foodEffectAmplifier(trinketLevel(WoodenSpoon.class));
    }

    public static int foodEffectAmplifier( int level ){
        if (level == -1){
            return -1;
        } else {
            return 1 + level;
        }
    }

    public static final LinkedHashMap<Class<? extends Food>, Class<? extends Food>[]> foodToVariant = new LinkedHashMap<>();

    private static final Class[] meatVariants = new Class[]{Ribs.class, DriedMeat.class};

    static {
        foodToVariant.put(Pasty.class,        new Class[]{Gruel.class, Pasta.class});
        foodToVariant.put(Food.class,         new Class[]{RyeBread.class, WheatBread.class});
        foodToVariant.put(SmallRation.class,  new Class[]{Honey.class, Bun.class});
        foodToVariant.put(MeatPie.class,      new Class[]{HuntersSandwich.class, TarteDeBry.class});
        foodToVariant.put(Berry.class,        new Class[]{GoldenBerry.class});
        foodToVariant.put(SupplyRation.class, new Class[]{SurvivalRation.class});
        foodToVariant.put(FrozenCarpaccio.class, meatVariants);
        foodToVariant.put(StewedMeat.class,      meatVariants);
        foodToVariant.put(ChargrilledMeat.class, meatVariants);
    }

}
