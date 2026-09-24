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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.armor.glyphs.curses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Clumsy extends Armor.Glyph {
	
	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		//no proc effect, triggers in Hero.actMove()
		return damage;
	}

	//more of a reduction really
	public static void movementProc( Char ch, int level ){
        if (level == -1 || ch.buff(MagicImmune.class) != null) return;

        // Normally 1
        // Ring of arcana +4 - 2
        // Ring of arcana +6 - 3
        int dist = (int) Math.floor( genericProcChanceMultiplier(ch) );

        PathFinder.buildDistanceMap(ch.pos, BArray.not( Dungeon.level.solid, null ), dist);

        for (Trap trap : Dungeon.level.traps.valueList()){
            if (trap != null && trap.active && PathFinder.distance[trap.pos] <= dist){

                // Do not trigger Tengu Dart Trap, unfair certain death
                // Gateway Trap have 33% chance to activate to prevent softlock
                if (!(trap instanceof TenguDartTrap) &&
                        (!(trap instanceof GatewayTrap) || Random.Int(3) == 0)) {
                    trap.trigger();
                }

                if (ch instanceof Hero && trap.disarmedByActivation) {
                    ((Hero) ch).interrupt();
                }
            }
        }

        for (Plant plant : Dungeon.level.plants.valueList()){
            if (plant != null && PathFinder.distance[plant.pos] <= dist){
                plant.trigger();
                if (ch instanceof Hero) {
                    ((Hero) ch).interrupt();
                }
            }
        }
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}
	
	@Override
	public boolean curse() {
		return true;
	}
}
