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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class MonkeyFist extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SLUNGSHOT;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
        DLY = 1f;

		tier = 2;
	}

    @Override
    public int STRReq() {
        return 10; // FIXME Delete this the done testing
    }

//    @Override
//    protected float baseDelay( Char owner ){
//        if (owner instanceof Hero && ((Hero)owner).STR() >= STRReq()) {
//            float inertia = 0;
//            Inertia i = owner.buff(Inertia.class);
//            if ( i != null ){
//                inertia = i.level;
//            }
//            return Math.max(0.5f, super.baseDelay(owner) * (1 - (0.1f * inertia)));
//        }
//        return super.baseDelay(owner);
//    }

    protected float speedMultiplier(Char owner ){
        Inertia i = owner.buff(Inertia.class);
        if ( i != null ){
            return Math.min(3f, super.speedMultiplier(owner) * (1 + (0.1f * i.level)));
        }
        return super.speedMultiplier(owner);
    }


    @Override
	public int max(int lvl) {
		return  4*(tier+1) +    //12 base, down from 15
				lvl*(tier+1);   //scaling unchanged
	}

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        Buff.affect(attacker,Inertia.class).extend();
        return super.proc(attacker, defender, damage);
    }

    public static class Inertia extends Buff{

        public int icon() {
            return BuffIndicator.FEROCITY;
        }

        int level = 0;

        public void extend(){
            clearTime();
            postpone(1f- target.cooldown());
            level +=1;
        }

        @Override
        public boolean act() {
            detach();
            return true;
        }

    }


    //	@Override
//	public int damageRoll(Char owner) {
//		if (owner instanceof Hero) {
//			Hero hero = (Hero)owner;
//			Char enemy = hero.attackTarget();
//			if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
//				//deals 67% toward max to max on surprise, instead of min to max.
//				int diff = max() - min();
//				int damage = augment.damageFactor(Hero.heroDamageIntRange(
//						min() + Math.round(diff*0.67f),
//						max()));
//				int exStr = hero.STR() - STRReq();
//				if (exStr > 0) {
//					damage += Hero.heroDamageIntRange(0, exStr);
//				}
//				return damage;
//			}
//		}
//		return super.damageRoll(owner);
//	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	public boolean useTargeting(){
		return false;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		Dagger.sneakAbility(hero, target, 4, 2+buffedLvl(), this);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", 2+buffedLvl());
		} else {
			return Messages.get(this, "typical_ability_desc", 2);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(2+level);
	}

}
