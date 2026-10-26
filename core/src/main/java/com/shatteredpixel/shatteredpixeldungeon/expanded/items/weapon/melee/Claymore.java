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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ExpandedWeaponAbilities;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Claymore extends MeleeWeapon {

	{
		image = ItemSpriteSheet.CLAYMORE;

		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.7f;

		tier = 5;
	}

    private boolean inProc = false; // Used to Avoid infinite loops
    private boolean inAbilityProc = false;

    @Override
    protected void beforeAbilityUsed(Hero hero, Char target) {
        super.beforeAbilityUsed(hero, target);
        inAbilityProc = true; // Not actually in proc, but we don't want to collateral attacks with the ability
    }

    @Override
    protected void afterAbilityUsed(Hero hero) {
        super.afterAbilityUsed(hero);
        inAbilityProc = false;
    }

    @Override
    public int max(int lvl) {
        return  4*(tier+1) +    //24 base, down from 30
                lvl*(tier+1);   //scaling unchanged
    }

    @Override
    public void hitSound(float pitch) {
        if (!inAbilityProc) super.hitSound(pitch);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if ( inProc || inAbilityProc ) return super.proc(attacker, defender, damage);

        ArrayList<Char> extraDefenders = new ArrayList<>();

        for (Mob mob : Dungeon.level.mobs){
            if (mob != attacker && // If this is a Mob (Ghost or Statue) it will be inside level.mobs, so avoid attacking yourself
                    mob != defender && // Do not collateral attack the char that you are already attacking
                    canColateralAttack(attacker, mob))
            {
                extraDefenders.add(mob);
            }
        }

        // The hero is not inside Dungeon.level.mobs, so we have to add him manually
        if (attacker instanceof Mob &&
                defender != Dungeon.hero &&
                canColateralAttack(attacker, Dungeon.hero))
        {
            extraDefenders.add(Dungeon.hero);
        }

        inProc = true;
        for (Char extraDefender : extraDefenders){
            attacker.attack(extraDefender, 0.67f, 0, 0.67f);
        }
        inProc = false;

        return super.proc(attacker, defender, damage);
    }

    private boolean canColateralAttack(Char attacker, Char defender){
        return defender.alignment != Char.Alignment.NEUTRAL &&
                attacker.alignment != defender.alignment &&
                attacker.fieldOfView[defender.pos] &&
                canReach(attacker, defender.pos);
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        int damageBoost = augment.damageFactor(10 + 10 * buffedLvl());
        ExpandedWeaponAbilities.pierceAbility(hero,target,1,damageBoost,this);
    }

    @Override
    public String upgradeAbilityStat(int level){
        int dmgBoost = 10 + 10 * level;
        return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
    }

    @Override
    public String abilityInfo() {
        int dmgBoost = levelKnown ? 10 + 10 * buffedLvl() : 10;
        if (levelKnown){
            return Messages.get(this,
                    "ability_desc",
                    augment.damageFactor(min()+dmgBoost),
                    augment.damageFactor(max()+dmgBoost),
                    1 + buffedLvl()
            );
        } else {
            return Messages.get(
                    this,
                    "typical_ability_desc",
                    min(0)+dmgBoost,
                    max(0)+dmgBoost,
                    1
            );
        }
    }

    public static final String IN_PROC = "in_proc";
    public static final String IN_ABILITY_PROC = "in_ability_proc";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(IN_PROC, inProc);
        bundle.put(IN_ABILITY_PROC, inAbilityProc);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        inProc = bundle.getBoolean(IN_PROC);
        inAbilityProc = bundle.getBoolean(IN_ABILITY_PROC);
    }

}
