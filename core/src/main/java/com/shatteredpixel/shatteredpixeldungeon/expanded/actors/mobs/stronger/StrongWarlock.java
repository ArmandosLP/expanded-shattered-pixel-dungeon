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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.ShadowWarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongWarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;


public class StrongWarlock extends Mob implements Callback {

	private static final float TIME_TO_ZAP              = 1f;
    private static final float TIME_TO_CREATE_SHADOW    = 1f;

	{
		spriteClass = StrongWarlockSprite.class;
		
		HP = HT = 70;
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Generator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Property.UNDEAD);

        HUNTING = new Hunting();
	}

    private int shadowID = -1;

    @Override
    public void die(Object cause) {
        super.die(cause);
        if (Char.findById(shadowID) != null){
            ((Shadow) Actor.findById(shadowID)).die(cause);
        }
    }

    public boolean doCreateShadow(int cell){
        if (sprite.visible || Dungeon.hero.fieldOfView[cell]){
            sprite.zap(cell, new Callback() { @Override public void call() {} }); // Do nothing, just visual attack
            Sample.INSTANCE.play( Assets.Sounds.ZAP );

            MagicMissile.boltFromChar( sprite.parent, MagicMissile.SHADOW, sprite, cell, new Callback() {
                @Override
                public void call() {
                    createShadow( cell );
                    CellEmitter.get(cell).burst(ShadowParticle.CURSE, 6);
                    Sample.INSTANCE.play(Assets.Sounds.CURSED);
                    next();
                }
            });
            return false;
        }
        createShadow(cell);
        return true;
    }
    private void createShadow(int cell){
        spend(TIME_TO_CREATE_SHADOW);

        Shadow lastShadow = (Shadow) Actor.findById(shadowID);
        if (lastShadow != null){ lastShadow.destroy(); }

        Shadow shadow = new Shadow();
        shadow.warlockID = this.id();

        this.shadowID = shadow.id();

        shadow.pos = cell;
        GameScene.add( shadow );
        Dungeon.level.occupyCell( shadow );

        //champion buff, mainly
        for (Buff b : buffs()){
            if (b.revivePersists) {
                Buff.affect(shadow, b.getClass());
            }
        }
    }

    private boolean isCellValid(int cell){
        return (fieldOfView[cell] &&
                Dungeon.level.passable[cell] &&
                Char.findChar(cell) == null &
                        (!Char.hasProp(this, Property.LARGE) && Dungeon.level.openSpace[cell]) // Mainly used for giant champions
        );
    }

    private int createShadowPos(Char target){
        int higherValue = 0;
        int cell = -1;

        for (int c = 0; c < fieldOfView.length; c++) {
            if ( !isCellValid(c) ) { continue; }

            int value = -1;

            int posToCell    = Dungeon.level.distance(pos, c);
            int targetToCell = Dungeon.level.distance(target.pos, c);

            if (
                    posToCell >= 2 &&
                    targetToCell >= 2 &&
                    (target != Dungeon.hero || Dungeon.hero.fieldOfView[c]) && // Make sure hero can see where shadow is placed
                    new Ballistica( pos,        c, Ballistica.MAGIC_BOLT).collisionPos == c &&
                    new Ballistica( c, target.pos, Ballistica.MAGIC_BOLT).collisionPos == target.pos
            ) {
                value = posToCell + targetToCell + targetToCell > posToCell ? 1 : 0;
            }

            if (value > higherValue) {
                higherValue = value;
                cell = c;
            }
        }
        return cell;
    }

    private Shadow shadowWarlock(){
        return (Shadow) Actor.findById(shadowID);
    }

    private class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;

            if (enemySeen && !isCharmedBy(enemy) && shadowWarlock() == null && !canAttack(enemy)) {
                int cell = createShadowPos(enemy);
                if (cell != -1) {
                    return doCreateShadow(cell);
                }
            }

            return super.act(enemyInFOV,justAlerted);
        }
    }

	@Override
	public int damageRoll() {
        return Random.NormalIntRange( 12, 18 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 25;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 8);
	}

    private Shadow getShadowForComboZap(){
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
            if (
                    mob instanceof Shadow &&
                            fieldOfView[mob.pos] &&
                            new Ballistica( pos, mob.pos, Ballistica.MAGIC_BOLT).collisionPos == mob.pos &&
                            new Ballistica( mob.pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos
            ){
                return (Shadow) mob;
            }
        }
        return null;
    }

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy) ||
                new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos ||
                getShadowForComboZap() != null;
	}

    // FIXME Refactor this
	protected boolean doAttack( Char enemy ) {
        if (Dungeon.level.adjacent( pos, enemy.pos )){
            return super.doAttack( enemy );
        }

        if (new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos){
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }

        Shadow shadow = getShadowForComboZap();

        if (shadow != null){
            if ((sprite != null && shadow.sprite != null) && (sprite.visible || enemy.sprite.visible || shadow.sprite.visible)) {
                ((StrongWarlockSprite) sprite).comboZap(shadow, enemy.pos);
                return false;
            }else{
                zap();
                return true;
            }
        }

        return super.doAttack( enemy );
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class DarkBolt{}
	
	protected void zap() {
		spend( TIME_TO_ZAP );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			//TODO would be nice for this to work on ghost/statues too
            if (enemy == Dungeon.hero && Random.Int( 2 ) == 0) {
                Buff.prolong( enemy, Degrade.class, Degrade.DURATION );
                Sample.INSTANCE.play( Assets.Sounds.DEGRADE );
            }
			
			int dmg = Random.NormalIntRange( 12, 18 );
			dmg = Math.round(dmg * AscensionChallenge.statModifier(this));

			//logic for DK taking 1/2 damage from aggression stoned minions
			if ( enemy.buff(StoneOfAggression.Aggression.class) != null
					&& enemy.alignment == alignment
					&& (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
				dmg *= 0.5f;
			}

			enemy.damage( dmg, new DarkBolt() );
			
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public void call() {
		next();
	}

	@Override
	public Item createLoot(){

		// 1/6 chance for healing, scaling to 0 over 8 drops
		if (Random.Int(3) == 0 && Random.Int(8) > Dungeon.LimitedDrops.WARLOCK_HP.count ){
			Dungeon.LimitedDrops.WARLOCK_HP.count++;
			return new PotionOfHealing();
		} else {
			Item i;
			do {
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
			} while (i instanceof PotionOfHealing);
			return i;
		}

	}

    @Override
    public String name() {
        return Messages.get(Warlock.class, "name");
    }

    @Override
    public String description() {
        return Messages.get(Warlock.class, "desc") + "\n\n" + Messages.get(this, "talent");
    }

    public static final String SHADOW_ID = "SHADOW_ID";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SHADOW_ID, shadowID);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        shadowID = bundle.getInt(SHADOW_ID);
    }

    public static class Shadow extends Mob {
        {
            spriteClass = ShadowWarlockSprite.class;

            defenseSkill = 0;
            HP = HT = 1;
            EXP = 0;

            properties.add(Property.IMMOVABLE);
            properties.add(Property.INORGANIC);

            WANDERING = new Wandering();
            state = WANDERING;
        }

        private int warlockID = -1;
        private int deathTimer = 3;

        private class Wandering extends Mob.Wandering {
            @Override
            public boolean act(boolean enemyInFOV, boolean justAlerted) {
                enemySeen = enemyInFOV;

                StrongWarlock master = (StrongWarlock) Char.findById(warlockID);

                if (master == null) { die( null ); }
                else if (!fieldOfView[master.pos]) { deathTimer -= 1; }
                else { deathTimer = 3; }

                if (deathTimer <= 0){
                    die(null);
                }

                spend(TICK);
                return true;
            }
        }

        public static final String WARLOCK_ID = "WARLOCK_ID";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(WARLOCK_ID, warlockID);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            warlockID = bundle.getInt(WARLOCK_ID);
        }

    }

}
