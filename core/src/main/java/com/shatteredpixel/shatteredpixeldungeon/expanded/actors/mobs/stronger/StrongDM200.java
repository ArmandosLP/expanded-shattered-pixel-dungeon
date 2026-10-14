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

package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongDM200Sprite;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class StrongDM200 extends DM200 {

	{
        spriteClass = StrongDM200Sprite.class;
	}

    private int energy = 0;

    @Override
    public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
        boolean result = super.attack(enemy, dmgMulti, dmgBonus, accMulti);
        boolean overcharged = isOvercharged();

        if (result && overcharged) {
            Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);

            enemy.damage(Random.IntRange(5, 8), new Shocking());
            enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
            enemy.sprite.flash();
            if (enemy instanceof Hero){
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail(this);
            }

        }

        if (overcharged) {
            Buff.prolong(this, Overcharge.class, Overcharge.DURATION);
        } else if (result) {
            energy += 1;
        }

        if (energy >= 3) {
            if (sprite != null && sprite.visible){
                Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
                ScrollOfRecharging.charge(this);
            }
            updateChargeState(true); // Emitter inside sprites is already checking for sprite visibility
            Buff.prolong(this, Overcharge.class, Overcharge.DURATION);
            energy = 0;
        }

        return result;
    }

    @Override
    protected boolean act() {
        if (enemy == null){
            energy = 0;
        }
        return super.act();
    }

    public void updateChargeState(boolean state){
        ((StrongDM200Sprite)sprite).updateChargeState(state);
    }

    public boolean isOvercharged(){
        return buff(Overcharge.class) != null;
    }

    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }

    private final static String ENERGY = "energy";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ENERGY, energy);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        energy = bundle.getInt(ENERGY);
    }

    public static class Overcharge extends FlavourBuff {

        public static final int DURATION = 3;

        {
            actPriority = MOB_PRIO - 1; // Act after the DM has acted
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.OVERCHARGE;
        }

        @Override
        public boolean attachTo(Char target) {
            if (!(target instanceof StrongDM200)){
                return false;
            }
            return super.attachTo(target);
        }

        @Override
        public void detach() {
            super.detach();
            if (target instanceof StrongDM200) {
                ((StrongDM200)target).updateChargeState(false);
            }
        }

    }
}











