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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class StrongBrute extends Brute {
	
	{
        HUNTING = new StrongBrute.Hunting();
	}

    private boolean attackOpportunity = false;

    private class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            boolean result = super.act(enemyInFOV, justAlerted);

            if (attackOpportunity && enemy != null && canAttack(enemy)){
                doAttack(enemy);
            }

            return result;
        }
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (super.doAttack(enemy)){
            attackOpportunity = false;
            return true;
        }
        return false;
    }

    @Override
    public void onAttackComplete() {
        super.onAttackComplete();
        attackOpportunity = false;
    }

    @Override
    public float attackDelay() {
        return attackOpportunity ? 0 : super.attackDelay();
    }

    @Override
    protected boolean getCloser(int target) {
        if (enemy == null || buff(BruteRage.class) == null) return super.getCloser(target);

        attackOpportunity = enemySeen && !canAttack(enemy);
        boolean result = super.getCloser(target);
        attackOpportunity = attackOpportunity && result;

        return result;
    }

    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }
}
