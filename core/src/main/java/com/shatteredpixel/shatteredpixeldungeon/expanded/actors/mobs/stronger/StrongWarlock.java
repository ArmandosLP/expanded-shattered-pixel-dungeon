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
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.ShadowWarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
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
    private static final float TIME_TO_SHADOW_SWITCH    = 1f;

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

    private boolean canCreateShadow(){
        return Actor.findById(shadowID) == null;
    }

    public boolean doShadowSwitch(ShadowWarlock shadowWarlock){
        if (sprite.visible || shadowWarlock.sprite.visible){
            sprite.zap(shadowWarlock.pos, new Callback() { @Override public void call() {} }); // Do nothing, just visual attack
            Sample.INSTANCE.play( Assets.Sounds.ZAP );

            MagicMissile.boltFromChar( sprite.parent, MagicMissile.ELMO, sprite, shadowWarlock.pos, new Callback() {
                @Override
                public void call() {
                    shadowSwitch(shadowWarlock);
                    next();
                }
            });
            return false;
        }
        shadowSwitch(shadowWarlock);
        return true;
    }

    private void shadowSwitch(ShadowWarlock shadowWarlock){
        spend(TIME_TO_SHADOW_SWITCH);

        int lastPos = this.pos;
        ScrollOfTeleportation.appear(this, shadowWarlock.pos);
        ScrollOfTeleportation.appear(shadowWarlock, lastPos);
    }
//        this.pos = shadowWarlock.pos;
//        this.sprite.place(shadowWarlock.pos);
//        Dungeon.level.occupyCell(this);
//        this.sprite.visible = Dungeon.level.heroFOV[shadowWarlock.pos];

//        shadowWarlock.pos = lastPos;
//        shadowWarlock.sprite.place(lastPos);
//        Dungeon.level.occupyCell(shadowWarlock);
//        shadowWarlock.sprite.visible = Dungeon.level.heroFOV[lastPos];

    /*
        // Not yet implemented properly
        if (Dungeon.isExpandedChallenged(ExpandedChallenges.STRONGER_MOBS)) {
            do {pos = level.pointToCell(random());
            } while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get(pos) != null);

            EnergySlime energySlime = new EnergySlime();
            energySlime.pos = pos;
            level.mobs.add(energySlime);
        }
*/


    public boolean doCreateShadow(int cell){
        if (sprite.visible || Dungeon.hero.fieldOfView[cell]){
            sprite.zap(cell, new Callback() { @Override public void call() {} }); // Do nothing, just visual attack
            Sample.INSTANCE.play( Assets.Sounds.ZAP );

            MagicMissile.boltFromChar( sprite.parent, MagicMissile.SHADOW, sprite, cell, new Callback() {
                @Override
                public void call() {
                    createShadow( cell );
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

        ShadowWarlock lastShadow = (ShadowWarlock) Actor.findById(shadowID);
        if (lastShadow != null){ lastShadow.destroy(); }

        ShadowWarlock shadow = new ShadowWarlock();
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

    private int evaluateCell(int cell, int enemyCell){
        if (!fieldOfView[cell]) { return -1; }
        if (!Dungeon.level.passable[cell]) { return -1; }
        if (Actor.findChar( cell ) != null) { return -1; }
        if (Char.hasProp(this, Property.LARGE) || !Dungeon.level.openSpace[cell]) { return -1; } // Mainly used for giant champions

        if (new Ballistica( pos, cell, Ballistica.MAGIC_BOLT).collisionPos != cell){ return -1; }

        int thisToCell = Dungeon.level.distance(pos, cell);
        if (thisToCell < 2){ return -1; }

        int enemyToCell = Dungeon.level.distance(enemyCell, cell);
        if (enemyToCell < 2){ return -1; }

        if (new Ballistica( cell, enemyCell, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos){ return -1; }

        return thisToCell + enemyToCell + enemyToCell > thisToCell ? 1 : 0;
    }

    // Can be this or other warlock
    // FIXME What if in dunger warlock is not visible for this warlock?
    private StrongWarlock inDangerWarlock(){
        for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
            if (
                    m instanceof StrongWarlock &&
                    !m.isCharmedBy(enemy) &&
                    m.isTargeting(enemy) &&
                    m.distance(enemy) == (m == this ? 2 : 1)
            ){
                return (StrongWarlock) m;
            }
        }
        return null;
    }


    private int optimalPosForEscape(StrongWarlock theEscapist){ // no way, the escapist reference!
        int bestValue = -1;
        int cell = -1;
        for (int c = 0; c < fieldOfView.length; c++){
            if (fieldOfView[c] && new Ballistica( pos, c, Ballistica.MAGIC_BOLT).collisionPos == c){
                int value = Dungeon.level.distance(c, enemy.pos);
                if (
                        value > bestValue &&
                        value > 2 && // Not worth the escape
                        // make sure the escapist can actually escape if it is not this
                        ( theEscapist == this || new Ballistica(theEscapist.pos, c, Ballistica.MAGIC_BOLT).collisionPos == c )
                )
                {
                    bestValue = value;
                    cell = c;
                }
            }
        }
        return cell;
    }

    private ShadowWarlock optimalEscapeShadow(){
        for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
            if (
                    m instanceof ShadowWarlock &&
                    fieldOfView[m.pos] &&
                    new Ballistica( pos, m.pos, Ballistica.MAGIC_BOLT).collisionPos == m.pos &&
                    m.distance(enemy) > 2
            ){
                return (ShadowWarlock) m;
            }
        }
        return null;
    }

    private class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;

            if (!justAlerted && enemySeen && distance(enemy) == 1){
                ShadowWarlock escapeShadow = optimalEscapeShadow();
                if (escapeShadow != null){
                    return doShadowSwitch(escapeShadow);
                }
            }

            if (enemySeen && !isCharmedBy(enemy) && canCreateShadow()) {
                // Tries to create shadow for later escape
                StrongWarlock inDangerWarlock = inDangerWarlock();
                if (inDangerWarlock != null){
                    int shadowPos = optimalPosForEscape(inDangerWarlock);
                    if (shadowPos != -1){
                        return doCreateShadow(shadowPos);
                    }
                }

                // Tries to create shadow for later escape
                if (!canAttack(enemy)){
                    int higherValue = 0;
                    int cell = -1;
                    for (int i = 0; i < fieldOfView.length; i++) {
                        int value = evaluateCell(i, enemy.pos);
                        if (value > higherValue) {
                            higherValue = value;
                            cell = i;
                        }
                    }
                    if (cell != -1) return doCreateShadow(cell);
                }


            }

            return super.act(enemyInFOV,justAlerted);
        }
    }

	@Override
	public int damageRoll() {
        return 3;
//		return Random.NormalIntRange( 12, 18 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 25;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 8);
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
        ShadowWarlock shadow = (ShadowWarlock) Actor.findById(shadowID);

        if (shadow != null){
            if (
                    new Ballistica( shadow.pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos &&
                    new Ballistica( pos, shadow.pos, Ballistica.MAGIC_BOLT).collisionPos == shadow.pos
            ){
                return true;
            }
        }

		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

    // FIXME Refactor this
	protected boolean doAttack( Char enemy ) {
        if (Dungeon.level.adjacent( pos, enemy.pos )){
            return super.doAttack( enemy );
        }

        ShadowWarlock shadow = (ShadowWarlock) Actor.findById(shadowID);

        if (new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos){
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }else if (
                shadow != null &&
                new Ballistica( shadow.pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos &&
                new Ballistica( pos, shadow.pos, Ballistica.MAGIC_BOLT).collisionPos == shadow.pos
        ){
            //multi zap logic
            if ( (sprite != null && shadow.sprite != null) && (sprite.visible || enemy.sprite.visible || shadow.sprite.visible) ) {

                sprite.zap(shadow.pos, new Callback() { @Override public void call() {} }); // Do nothing, just visual

                Sample.INSTANCE.play( Assets.Sounds.ZAP );
                MagicMissile.boltFromChar(
                        sprite.parent,
                        MagicMissile.SHADOW,
                        sprite,
                        shadow.pos,
                        new Callback() {
                            @Override
                            public void call() {
                                Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1, 1.5f );

                                MagicMissile.boltFromChar(
                                        shadow.sprite.parent,
                                        MagicMissile.SHADOW,
                                        shadow.sprite,
                                        enemy.pos,
                                        new Callback() {
                                            @Override
                                            public void call() {
                                                zap();
                                                next();
                                            }
                                        } );
                            }
                        } );
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

    public static class ShadowWarlock extends Mob {
        {
            spriteClass = ShadowWarlockSprite.class;

            HP = HT = 1;
            EXP = 0;

            properties.add(Property.IMMOVABLE);
            properties.add(Property.INORGANIC);

            WANDERING = new Wandering();
            state = WANDERING;
        }

        private int warlockID = -1;

        private class Wandering extends Mob.Wandering {
            @Override
            public boolean act(boolean enemyInFOV, boolean justAlerted) {
                enemySeen = enemyInFOV;
                spend(TICK);
                return true;
            }
        }

        @Override
        public int defenseSkill(Char enemy) {
            return INFINITE_EVASION;
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
