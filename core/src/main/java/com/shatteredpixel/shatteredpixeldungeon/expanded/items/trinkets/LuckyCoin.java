
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

import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class LuckyCoin extends Trinket {

    {
        image = ItemSpriteSheet.LUCKY_COIN;
    }


    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(
                    this,
                    "stats_desc",
                    Messages.decimalFormat(
                            "#.##",
                            100 * (goldMultiplier(buffedLvl()) - 1f)),
                    Messages.decimalFormat(
                            "#.##",
                            100 * (goldMultiplier(buffedLvl()) - 1f)),
                    extraShopItems(buffedLvl()));
        } else {
            return Messages.get(
                    this,
                    "typical_stats_desc",
                    Messages.decimalFormat(
                            "#.##",
                            100 * (goldMultiplier(0) - 1f)),
                    Messages.decimalFormat(
                            "#.##",
                            100 * (goldMultiplier(0) - 1f)),
                    extraShopItems(0));
        }
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 10(16) -> 15(31) -> 20(51)
        return 10+5*level();
    }

    public static float goldMultiplier(){
        return goldMultiplier(trinketLevel(LuckyCoin.class));
    }

    public static int extraShopItems(){
        return extraShopItems(trinketLevel(LuckyCoin.class));
    }

    public static float goldenFoodChance(){
        return goldenFoodChance(trinketLevel(LuckyCoin.class));
    }

    public static float goldMultiplier( int level ){
        if (level == -1){
            return 1f;
        } else {
            return 1.1f + 0.05f*level;
        }
    }

    public static int extraShopItems( int level ){
        if (level == -1){
            return 0;
        } else {
            return 2 + 2*level;
        }
    }

    public static float goldenFoodChance( int level ){
        if (level == -1){
            return 0f;
        } else {
            return 0.1f + 0.05f*level;
        }
    }
}
