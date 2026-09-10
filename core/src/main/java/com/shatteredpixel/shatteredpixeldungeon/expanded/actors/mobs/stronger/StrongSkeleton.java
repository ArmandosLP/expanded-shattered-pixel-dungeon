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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.RyeBread;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.food.WheatBread;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongSkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class StrongSkeleton extends Mob {
    public int weaponDurability;

	{
		spriteClass = SkeletonSprite.class;

		HP = HT = 25;
		defenseSkill = 9;
		
		EXP = 5;
		maxLvl = 10;

		loot = Generator.Category.WEAPON;
		lootChance = 0.1667f; //by default, see lootChance()

        properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);

        weaponDurability = 0;
	}

    @Override
    protected void onAdd() {
        super.onAdd();
        System.out.println(sprite);
    }

    @Override
	public int damageRoll() {
		return Random.NormalIntRange( 2, 10 );
	}

    @Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		if (cause == Chasm.class) return;
		
		boolean heroKilled = false;
		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			Char ch = findChar( pos + PathFinder.NEIGHBOURS8[i] );
			if (ch != null && ch.isAlive()) {
				int damage = Math.round(Random.NormalIntRange(6, 12));
				damage = Math.round( damage * AscensionChallenge.statModifier(this));

				//all sources of DR are 2x effective vs. bone explosion
				//this does not consume extra uses of rock armor and earthroot armor

				WandOfLivingEarth.RockArmor rockArmor = ch.buff(WandOfLivingEarth.RockArmor.class);
				if (rockArmor != null) {
					int preDmg = damage;
					damage = rockArmor.absorb(damage);
					damage *= Math.round(damage/(float)preDmg); //apply the % reduction twice
				}

				Earthroot.Armor armor = ch.buff( Earthroot.Armor.class );
				if (damage > 0 && armor != null) {
					int preDmg = damage;
					damage = armor.absorb( damage );
					damage -= (preDmg - damage); //apply the flat reduction twice
				}

                WheatBread.CarbohydrateRush wheatCarbRush = buff(WheatBread.CarbohydrateRush.class);
                if (wheatCarbRush != null){
                    // Static damage reduction, do not apply the reduction twice.
                    damage = wheatCarbRush.absorb( damage );
                }

                RyeBread.CarbohydrateRush ryeCarboRush = buff( RyeBread.CarbohydrateRush.class );
                if (damage > 0 && ryeCarboRush != null) {
                    int preDmg = damage;
                    damage = ryeCarboRush.absorb( damage );
                    damage -= (preDmg - damage); //apply the flat reduction twice
                }

				if (ch.buff(MagicImmune.class) == null) {
					ShieldOfLight.ShieldOfLightTracker shield = ch.buff(ShieldOfLight.ShieldOfLightTracker.class);
					if (shield != null && shield.object == id()) {
						int min = 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT);
						damage -= Random.NormalIntRange(min, 2 * min);
						damage -= Random.NormalIntRange(min, 2 * min); //apply twice
						damage = Math.max(damage, 0);
					} else if (ch == Dungeon.hero
							&& Dungeon.hero.heroClass != HeroClass.CLERIC
							&& Dungeon.hero.hasTalent(Talent.SHIELD_OF_LIGHT)
							&& TargetHealthIndicator.instance.target() == this) {
						//33/50%
						if (Random.Int(6) < 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT)) {
							damage -= 2; //doubled
						}
					}

					if (ch.buff(HolyWard.HolyArmBuff.class) != null){
						//doubled
						damage -= Dungeon.hero.subClass == HeroSubClass.PALADIN ? 6 : 2;
					}
				}

				//apply DR twice (with 2 rolls for more consistency)
				damage = Math.max( 0,  damage - (ch.drRoll() + ch.drRoll()) );
				ch.damage( damage, this );
				if (ch == Dungeon.hero && !ch.isAlive()) {
					heroKilled = true;
				}
			}
		}
		
		if (Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play( Assets.Sounds.BONES );
		}
		
		if (heroKilled) {
			Dungeon.fail( this );
			GLog.n( Messages.get(Skeleton.class, "explo_kill") );
		}
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/6, 1/18, 1/54, 1/162, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.SKELE_WEP.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SKELE_WEP.count++;
		return super.createLoot();
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}

    public static Class<? extends StrongSkeleton> random(){
        float roll = Random.Float();
        if (roll < 0.25f){
            return StrongSkeleton.Spear.class;
        } else if (roll < 0.50f){
            return StrongSkeleton.Sword.class;
        } else if (roll < 0.75f){
            return StrongSkeleton.Shield.class;
        }else{
            return StrongSkeleton.class;
        }
    }

    public void breakWeapon(int durability){
        weaponDurability -= durability;
        if (weaponDurability <= 0){ breakWeapon(); }
    }

    public void breakWeapon(){
        weaponDurability = 0;
        ((StrongSkeletonSprite) sprite).weaponBreak();
    }

    @Override
    public String name() {
        return Messages.get(Skeleton.class,"name");
    }

    @Override
    public String description() {
        String desc = Messages.get(Skeleton.class,"desc");
        if (weaponDurability > 0) { desc += "\n\n" + Messages.get(this,"talent"); }
        return desc;
    }

    @Override
    public CharSprite sprite() {
        if (weaponDurability == 0){ return Reflection.newInstance( SkeletonSprite.class ); }
        return Reflection.newInstance(spriteClass);
    }

    public static final String WEAPON_DURABILITY = "weapon_durability";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(WEAPON_DURABILITY, weaponDurability);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        weaponDurability = bundle.getInt(WEAPON_DURABILITY);
    }

    public static class Spear extends StrongSkeleton{
        {
            spriteClass = StrongSkeletonSprite.Spear.class;
            weaponDurability = 3;
        }

        @Override
        public int damageRoll() {
            if (weaponDurability > 0) {
                return Random.NormalIntRange(2, 6);
            }else {
                return super.damageRoll();
            }
        }

        @Override
        public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
            boolean result = super.attack(enemy, dmgMulti, dmgBonus, accMulti / 0.5f); // half damage to spear attacks
            if (result && weaponDurability > 0) { breakWeapon(1); }
            return result;
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            if (Dungeon.level.adjacent( pos, enemy.pos )){
                return true;
            }

            if (weaponDurability > 0 && Dungeon.level.distance( pos, enemy.pos ) <= 2){
                boolean[] passable = BArray.not(Dungeon.level.solid, null);

                for (Char ch : Actor.chars()) {
                    //our own tile is always passable
                    passable[ch.pos] = ch == this;
                }

                PathFinder.buildDistanceMap(enemy.pos, passable, 2);

                if (PathFinder.distance[pos] <= 2){
                    return true;
                }
            }

            return super.canAttack(enemy);
        }

    }

    public static class Shield extends StrongSkeleton{
        {
            spriteClass = StrongSkeletonSprite.Shield.class;
            weaponDurability = 2;
        }

        @Override
        public CharSprite sprite() {
//            if (weaponDurability > 0){ return Reflection.newInstance( this.spriteClass); }
            System.out.println(spriteClass);
            return Reflection.newInstance(spriteClass);
        }

        boolean shieldProc = false; // Used to connect defenseSkill() and defenseVerb()

        @Override
        public int defenseSkill( Char enemy ) {
            if (state == HUNTING && Random.IntRange(0, weaponDurability) != 0){
                breakWeapon(1);
                shieldProc = true;
                return INFINITE_EVASION;
            }
            return super.defenseSkill( enemy );
        }

        @Override
        public String defenseVerb() {
            if (shieldProc) {
                shieldProc = false;
                if (sprite != null && sprite.visible && weaponDurability != 0) {
                    Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(1.1f, 1.2f));
                }
                return Messages.get(Monk.class, "parried"); // FIXME maybe different message for shield parry
            }
            return super.defenseVerb();
        }

    }

    public static class Sword extends StrongSkeleton{
        {
            spriteClass = StrongSkeletonSprite.Sword.class;
            weaponDurability = 2;
        }

        @Override
        public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
            boolean result = super.attack(enemy, dmgMulti, dmgBonus, accMulti / 0.5f); // half damage to sword attacks
            if (weaponDurability > 0 && result) {
                if (enemy != null && enemy.isAlive()) Buff.affect( enemy, Bleeding.class).set( ((float) damageRoll()) / 2 );
                breakWeapon(1);
            }
            return result;
        }

        public void hitSound( float pitch ){
            if (weaponDurability > 0) {
                Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH);
            }else{
                super.hitSound(pitch);
            }
        }

    }

}
