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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.ShamanBlessing;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongShamanSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class StrongShaman extends Mob {
	
	{
        HP = HT = 5;
        defenseSkill = 1;

//		HP = HT = 35;
//		defenseSkill = 15;
		
		EXP = 8;
		maxLvl = 16;
		
		loot = Generator.Category.WAND;
		lootChance = 0.03f; //initially, see lootChance()

        HUNTING = new StrongShaman.Hunting();
	}

    private boolean hasShamanBless(Char ch){
        if (ch.buff(ShamanBlessing.Red.class)    != null) return true;
        if (ch.buff(ShamanBlessing.Blue.class)   != null) return true;
        if (ch.buff(ShamanBlessing.Purple.class) != null) return true;
        return false;
    }

    private ArrayList<Char> availableAllies(){
        // Returns allays that are targeting the same enemy as this mob
        // Can return hero if charmed
        ArrayList<Char> accessibleChars = new ArrayList<>();

        if (enemy == null) return accessibleChars;
        // Only hero if charmed, ignore other chars
        if ((isCharmedBy(Dungeon.hero) || buff(ScrollOfSirensSong.Enthralled.class) != null) &&
                !hasShamanBless(Dungeon.hero) &&
                new Ballistica(pos, Dungeon.hero.pos, Ballistica.MAGIC_BOLT).collisionPos == Dungeon.hero.pos) {
            accessibleChars.add(Dungeon.hero);
            return accessibleChars;
        }

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (fieldOfView[mob.pos] &&                                   // Can see
                    !hasShamanBless(mob) &&                               // Doesn't have shamans blessing.
                    mob.alignment == Alignment.ENEMY &&                   // Is enemy (Not neutral so can't target mimics)
                    !mob.isCharmedBy(enemy) &&                            // Is your ally
                    !mob.properties().contains(Property.IMMOVABLE) &&     // Not immovable
                    mob.isTargeting(enemy) && mob.state == mob.HUNTING && // Is hunting the same target as you
                    new Ballistica(pos, mob.pos, Ballistica.MAGIC_BOLT).collisionPos == mob.pos && // Accessible by projectile
                    mob != this                                           // Not you, damn it
            ) { accessibleChars.add(mob); }
        }
        return accessibleChars;
    }

    private class Hunting extends Mob.Hunting{
        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            enemySeen = enemyInFOV;
            ArrayList<Char> availableAllies = availableAllies();

            if (enemyInFOV && !isCharmedBy( enemy ) && !canAttack( enemy ) && !availableAllies.isEmpty()) {
                Char randAllay = Random.element(availableAllies);
                if ((sprite.visible || randAllay.sprite.visible)){
                    ((StrongShamanSprite) sprite).buff(randAllay.pos);
                    return false;
                }else{
                    buffChar(randAllay);
                    return true;
                }
            }
            return super.act( enemyInFOV, justAlerted );
        }
    }

    @Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 10 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 18;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 6);
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/33, 1/100, 1/300, 1/900, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.SHAMAN_WAND.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SHAMAN_WAND.count++;
		return super.createLoot();
	}

	protected boolean doAttack(Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			return super.doAttack( enemy );
		} else {
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class EarthenBolt{}

    private void buffChar(Char ch) {
        spend( 1f );
        Invisibility.dispel(this);

        if (ch != null && ch.isAlive()) {
            applyBuff(ch);
        }
    }

	private void zap() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			
			if (Random.Int( 2 ) == 0) {
				debuff( enemy );
				if (enemy == Dungeon.hero) Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
			}
			
			int dmg = Random.NormalIntRange( 6, 15 );
			dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
			enemy.damage( dmg, new EarthenBolt() );

			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(Shaman.class, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}

    public void onZapComplete() {
        zap();
        next();
    }

    public void onBuffComplete(int cell) {
        buffChar(Actor.findChar(cell));
        next();
    }
	protected abstract void debuff( Char enemy );

    protected abstract void applyBuff( Char allay );

    @Override
    public String name() {
        return Messages.get(Shaman.class, "name");
    }

	@Override
	public String description() {
		return Messages.get(Shaman.class, "desc") + "\n\n" + Messages.get(this, "spell_desc");
	}

	public static class RedShaman extends StrongShaman {
		{
			spriteClass = StrongShamanSprite.Red.class;
		}
		
		@Override
		protected void debuff( Char enemy ) {
			Buff.prolong( enemy, Weakness.class, Weakness.DURATION );
		}

        @Override
        protected void applyBuff( Char allay ) {
            Buff.prolong( allay, ShamanBlessing.Red.class, ShamanBlessing.DURATION );
            SpellSprite.show( allay, SpellSprite.RED_SHAMAN_BLESSING);
        }

    }
	
	public static class BlueShaman extends StrongShaman {
		{
			spriteClass = StrongShamanSprite.Blue.class;
		}
		
		@Override
		protected void debuff( Char enemy ) {
			Buff.prolong( enemy, Vulnerable.class, Vulnerable.DURATION );
		}

        @Override
        protected void applyBuff( Char allay ) {
            Buff.prolong( allay, ShamanBlessing.Blue.class, ShamanBlessing.DURATION );
            SpellSprite.show( allay, SpellSprite.BLUE_SHAMAN_BLESSING);
        }

	}
	
	public static class PurpleShaman extends StrongShaman {
		{
			spriteClass = StrongShamanSprite.Purple.class;
		}
		
		@Override
		protected void debuff( Char enemy ) {
			Buff.prolong( enemy, Hex.class, Hex.DURATION );
		}

        @Override
        protected void applyBuff( Char allay ) {
            Buff.prolong( allay, ShamanBlessing.Purple.class, ShamanBlessing.DURATION );
            SpellSprite.show( allay, SpellSprite.PURPLE_SHAMAN_BLESSING);
        }

	}
	
	public static Class<? extends StrongShaman> random(){
		float roll = Random.Float();
		if (roll < 0.4f){
			return RedShaman.class;
		} else if (roll < 0.8f){
			return BlueShaman.class;
		} else {
			return PurpleShaman.class;
		}
	}
}
