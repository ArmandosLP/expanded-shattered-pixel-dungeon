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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Vitality extends Armor.Glyph {

	private static final ItemSprite.Glowing VIVID_RED = new ItemSprite.Glowing( 0xFF3D3D );

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
        int level = Math.max( 0, armor.buffedLvl() );

        // lvl 0 - 10%
        // lvl 1 - 18%
        // lvl 2 - 25%
        // if active Retained Vitality - 100%
        float procChance = (level+1f)/(level+10f) * procChanceMultiplier(defender);
        if (defender.buff(RetainedVitality.class) != null || Random.Float() < procChance) {
            Sample.INSTANCE.play(Assets.Sounds.DRINK, 1, Random.Float(1.8f, 2f));
            Sample.INSTANCE.play(Assets.Sounds.BADGE, 0.5f, Random.Float(0.8f, 1f));
            defender.sprite.emitter().burst(BloodParticle.BURST, 8);

            float powerMulti = Math.max(1f, procChance);
            Buff.affect(defender, RetainedVitality.class).extend( (int) (Math.max(damage, 1) / 2 * powerMulti) );
        }

		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {return VIVID_RED;}

    public static class RetainedVitality extends Buff {

        {
            type = buffType.POSITIVE;
        }

        public void extend(int level){
            this.level += level;
            detach = false;
            if (this.level <= 0){
                detach();
            }
        }

        @Override
        public String desc() {
            return Messages.get(this,"desc", level);
        }

        private int level = 0;
        private boolean detach = false;

        public void proc(int effectiveDamage){
            if (target == null || !target.isAlive()){
                return;
            }

            int heal = Math.min(level,effectiveDamage);
            int finalHP = Math.min(target.HP + heal, target.HT);
            int effectiveHP = finalHP - target.HP;

            if (effectiveHP > 0) {
                target.HP = finalHP;
                target.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(effectiveHP), FloatingText.HEALING);
            }

            detach();
        }

        @Override
        public boolean act() {
            if (detach){
                detach();
            }else{
                detach = true;
            }
            spend(TICK);
            return true;
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(level);
        }

        @Override
        public int icon() {
            return BuffIndicator.RETAINED_VITALITY;
        }

        private static final String LEVEL = "level";
        private static final String DETACH = "level";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEVEL,level);
            bundle.put(DETACH,detach);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            level = bundle.getInt(LEVEL);
            detach = bundle.getBoolean(DETACH);
        }
    }

}
