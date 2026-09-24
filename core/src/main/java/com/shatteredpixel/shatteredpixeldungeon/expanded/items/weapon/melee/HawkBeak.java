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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MorningStar;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class HawkBeak extends MeleeWeapon {

	{
		image = ItemSpriteSheet.HAWKBEAK;
		hitSoundPitch = 1f;

		tier = 4;
	}

    @Override
    public void hitSound(float pitch) {
        switch (Random.Int(3)){
            case 0:
                hitSound = Assets.Sounds.HIT_SLASH;
                break;
            case 1:
                hitSound = Assets.Sounds.HIT_STAB;
                break;
            case 2:
                hitSound = Assets.Sounds.HIT_CRUSH;
                break;
        }
        super.hitSound(pitch);
    }

    @Override
    public int min(int lvl) {
        return max(lvl) - (lvl / 2); //same as max with 1 less for every 2 levels
    }

    @Override
    public int max(int lvl) {
        return  (4 * tier) - 1 +  //base: 15 dmg
                ((int) (lvl*2.5f));   //level scaling: 2 or 3 dmg
    }


	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        //+(2+lvl) damage, bonus damage is low because paralysis already gives a free hit
        int dmgBoost = augment.damageFactor(2 + buffedLvl());
        MorningStar.headBlowAbility(hero, target, 1, dmgBoost, this);
    }

    @Override
    public String upgradeAbilityStat(int level){
        int dmgBoost = 2 + level;
        return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
    }

    @Override
    public String abilityInfo() {
        int dmgBoost = levelKnown ? 2 + buffedLvl() : 2;
        if (levelKnown){
            return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
        } else {
            return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
        }
    }

}
