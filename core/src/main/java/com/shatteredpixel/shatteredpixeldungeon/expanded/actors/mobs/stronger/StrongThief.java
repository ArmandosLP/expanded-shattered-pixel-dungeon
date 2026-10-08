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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class StrongThief extends Thief {

    @Override
    public void damage(int dmg, Object src) {
        if (state == FLEEING && src instanceof Char && canAttack((Char) src)){
            Buff.affect(this,Counter.class).set((Char) src);
        }
        super.damage(dmg, src);
    }

    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this,"talent");
    }

    public static class Counter extends Buff {

        {
            type = buffType.POSITIVE;
            actPriority = VFX_PRIO;
            announced = false;
        }

        public void set(Char toAttack){
            this.toAttackID = toAttack.id();
        }

        private int toAttackID = -1;

        @Override
        public boolean act() {
            Char toAttack = (Char) findById(toAttackID);
            if (toAttack == null){
                detach();
                return true;
            }
            return doCounter(toAttack);
        }

        protected boolean doCounter( Char enemy ) {
            if (target.sprite != null && (target.sprite.visible || enemy.sprite.visible)) {
                target.sprite.attack(enemy.pos, new Callback() {
                    @Override
                    public void call() {
                        target.attack( enemy , 0.3f, 0, 1f);
                        Invisibility.dispel(target);
                        detach();
                        next();
                    }
                });
                return false;
            } else {
                target.attack( enemy , 0.3f, 0, 1f);
                Invisibility.dispel(target);
                detach();
                return true;
            }
        }

        public static final String TO_ATTACK_ID = "to_attack_id";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(TO_ATTACK_ID, toAttackID);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            toAttackID = bundle.getInt(TO_ATTACK_ID);
        }
    }

}
