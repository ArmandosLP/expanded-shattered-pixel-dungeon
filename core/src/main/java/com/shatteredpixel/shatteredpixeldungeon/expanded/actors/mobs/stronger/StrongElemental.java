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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public abstract class StrongElemental extends Elemental {

    public static class StrongFireElemental extends StrongElemental.FireElemental {

        @Override
        public void damage(int dmg, Object src) {
            // Resistant to magic, weak to missile weapons
            if (AntiMagic.RESISTS.contains(src.getClass())){
                dmg /= 2;
                rangedCooldown = 0;
            }else if (src == Dungeon.hero && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
                dmg += 5;
            }
            super.damage(dmg, src);
        }

        @Override
        public String description() {
            return super.description() + "\n\n" + Messages.get(this, "talent");
        }

    }

	public static class StrongFrostElemental extends StrongElemental.FrostElemental {

        @Override
        public void damage(int dmg, Object src) {
            // Resistant to missile weapons, weak to melee weapons
            if (src == Dungeon.hero && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
                dmg /= 2;
            }else if (src instanceof Char && !(src == Dungeon.hero && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon)){
                dmg += 5;
            }
            super.damage(dmg, src);
        }

        @Override
        public String description() {
            return super.description() + "\n\n" + Messages.get(this, "talent");
        }

	}
	
	public static class StrongShockElemental extends StrongElemental.ShockElemental {

        @Override
        public void damage(int dmg, Object src) {
            // Resistant to melee weapons, weak to magic
            if (src instanceof Char && !(src == Dungeon.hero && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon)){
                //Shock penalty in defenseproc (only for the hero)
                dmg /= 2;
            }else if (AntiMagic.RESISTS.contains(src.getClass())){
                dmg += 5;
            }
            super.damage(dmg, src);
        }

        @Override
        public int defenseProc(Char enemy, int damage) {
            if (enemy == Dungeon.hero && !(Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon)){
                enemy.sprite.parent.addToFront( new Lightning( sprite.center(), enemy.sprite.center(), null ) );
                enemy.damage( Random.IntRange(1, 5), new Shocking() );
                Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
                enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
                enemy.sprite.flash();
                if (!enemy.isAlive()){
                    Badges.validateDeathFromEnemyMagic();
                    Dungeon.fail(this);
                }
            }
            return super.defenseProc(enemy, damage);
        }

        @Override
        public String description() {
            return super.description() + "\n\n" + Messages.get(this, "talent");
        }

    }
	
	public static Class<? extends Elemental> random(){
		float altChance = 1/50f * RatSkull.exoticChanceMultiplier();
		if (Random.Float() < altChance){
			return ChaosElemental.class;
		}
		
		float roll = Random.Float();
		if (roll < 0.4f){
			return StrongFireElemental.class;
		} else if (roll < 0.8f){
			return StrongFrostElemental.class;
		} else {
			return StrongShockElemental.class;
		}
	}
}
