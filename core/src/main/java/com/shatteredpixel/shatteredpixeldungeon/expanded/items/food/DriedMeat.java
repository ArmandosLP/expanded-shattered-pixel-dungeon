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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class DriedMeat extends ChargrilledMeat {

	{
		image = ItemSpriteSheet.DRIED_MEAT;
		energy = Hunger.HUNGRY/2f;
	}

    @Override
    protected void satisfy( Hero hero ){
        super.satisfy(hero);

        if (WoodenSpoon.foodEffectAmplifier() == -1) return;

        int amplifier = WoodenSpoon.foodEffectAmplifier();

        // 1 -> 50% 1/2 -> 2 -> 50% 2/3
        for (int i = 0; i < (amplifier + 1) / 2; i++) {
            randomBuff(hero);
        }

        if (amplifier % 2 == 0 && Random.Int(2) == 0) {
            randomBuff(hero);
        }
    }

    @Override
    public String desc() {
        String desc = super.desc();
        desc += "\n\n" + Messages.get(WoodenSpoon.class,"function",Messages.get(getClass().getSuperclass(),"name"));

        if (WoodenSpoon.foodEffectAmplifier() != -1){
            desc += "\n\n" + Messages.get(WoodenSpoon.class,"react") + "\n";

            int amplifier = WoodenSpoon.foodEffectAmplifier();

            if (amplifier % 2 == 1) {
                // Guaranteed description
                desc += "\n" + Messages.get(this,"spoon_buff", WoodenSpoon.foodEffectAmplifier() / 2 + 1);
            }else{
                // 50% extra description
                desc += "\n" + Messages.get(this,"spoon_buff_variant", WoodenSpoon.foodEffectAmplifier() / 2, WoodenSpoon.foodEffectAmplifier() / 2 + 1);
            }

        }
        return desc;
    }

    public void randomBuff(Hero hero){
        switch (Random.Int( 5 )) {
            case 0:
                GLog.i( Messages.get(this, "inspier"));
                Buff.affect( hero, Recharging.class, 10);
                break;
            case 1:
                GLog.i( Messages.get(this, "energy") );
                Buff.affect( hero, Swiftthistle.TimeBubble.class).reset();
                break;
            case 2:
                GLog.i( Messages.get(this, "better") );
                hero.HP = Math.min( hero.HP + hero.HT / 4, hero.HT );
                hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(hero.HT / 4), FloatingText.HEALING );
                break;
            case 3:
                GLog.i( Messages.get(this, "mindvis") );
                Buff.affect( hero, MindVision.class, MindVision.DURATION );
                break;
            case 4:
                GLog.i( Messages.get(this, "refresh") );
                PotionOfHealing.cure(hero);
                break;
        }
    }

}
