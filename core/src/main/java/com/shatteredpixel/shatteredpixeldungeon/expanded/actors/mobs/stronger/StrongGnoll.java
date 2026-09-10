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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlockTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WornDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.noosa.audio.Sample;

import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class StrongGnoll extends Gnoll {

    private static final float TIME_TO_ZAP	= 1f;

	{
        HUNTING = new StrongGnoll.Hunting();
	}

    private class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;

            if (enemyInFOV && !isCharmedBy(enemy) && !canAttack(enemy)) {
                Trap trap = findTrap();
                if (trap != null) {
                    if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                        Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);

                        // Using attack animation for throw stone
                        sprite.attack(trap.pos, new Callback() {
                            @Override
                            public void call() { } // Do nothing
                        });

                        // Throwing stones animation
                        ((MissileSprite)sprite.parent.recycle( MissileSprite.class )).reset( sprite, trap.pos, new GnollStone(), new Callback() {
                            @Override
                            public void call() {
                                throwStone(trap.pos);
                                next();
                            }} );

                        return false;
                    } else {
                        throwStone(trap.pos);
                        return true;
                    }
                }
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }

    protected void throwStone(int trap_pos) {
        spend( TIME_TO_ZAP );
        Invisibility.dispel(this);
        mobTriggerTrap(this, Dungeon.level.traps.get(trap_pos));
    }

    // Manually applies the buffs of blob generating traps activated by enemies.
    // This avoids modifying blob.seed().
    public static void mobTriggerTrap(Mob mob, Trap trap){
        if (trap == null || !trap.active) return;

        trap.trigger();

        // Shocking and Toxic Traps don't need special logic
        if (trap instanceof ChillingTrap){
            for( int i : PathFinder.NEIGHBOURS9) {
                Freezing.freeze(trap.pos + i);
            }
        }else if (trap instanceof BurningTrap){
            for(int i : PathFinder.NEIGHBOURS9) {
                Fire.burn(trap.pos + i);
            }
        }else if (trap instanceof TeleportationTrap || trap instanceof GatewayTrap){
            Buff.affect(mob, Healing.class).setHeal(8,0.25f,0);
        }
    }

    private Trap findTrap(){
        for (Trap trap: Dungeon.level.traps.valueList()){
            if (!trap.active) continue;
            if (!trap.visible) continue;
            if (!fieldOfView[trap.pos]) continue;
            if (new Ballistica(pos, trap.pos, Ballistica.PROJECTILE).collisionPos != trap.pos) continue;

            // Enemy closer than self to trap
            if (trap instanceof WornDartTrap || trap instanceof PoisonDartTrap){
                if (Dungeon.level.distance( trap.pos, enemy.pos ) < Dungeon.level.distance( trap.pos, pos )){
                    return trap;
                }
            }

            // Enemy close to trap, self far from
            if (trap instanceof ConfusionTrap || trap instanceof FlockTrap || trap instanceof ToxicTrap){
                if (Dungeon.level.distance( trap.pos, enemy.pos ) < 3 && Dungeon.level.distance( trap.pos, pos ) > 5){
                    return trap;
                }
            }

            // Enemy adjacent to trap, self not adjacent to trap
            if (trap instanceof ChillingTrap || trap instanceof ShockingTrap || trap instanceof OozeTrap || trap instanceof BurningTrap || trap instanceof ExplosiveTrap){
                if (!Dungeon.level.adjacent(pos, trap.pos) && Dungeon.level.adjacent(enemy.pos, trap.pos)){
                    return trap;
                }
            }

            // Trap and enemy visible
            if (trap instanceof SummoningTrap || trap instanceof AlarmTrap){
                if (!isCharmedBy( enemy )) return trap; // Do not activate the traps of charmed
            }

            // self or enemy adjacent to Trap and self low hp
            if (trap instanceof TeleportationTrap || trap instanceof GatewayTrap){
                if (HT / 5 < 4 && Dungeon.level.adjacent(pos, trap.pos) || Dungeon.level.adjacent(enemy.pos, trap.pos)) {
                    return trap;
                }
            }
        }
        return null;
    }

    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }

    public static class GnollStone extends Item {
        {
            image = ItemSpriteSheet.THROWING_STONE;
        }
    }

}
