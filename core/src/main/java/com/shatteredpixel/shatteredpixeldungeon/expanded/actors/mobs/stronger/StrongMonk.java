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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.expanded.items.trinkets.WoodenSpoon;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongMonkSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class StrongMonk extends Mob {

    private float energy = 0f;

	{
		spriteClass = StrongMonkSprite.class;

        HP = HT = 70;
        defenseSkill = 30;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Food.class;
		lootChance = 0.083f;

		properties.add(Property.UNDEAD);

        HUNTING = new StrongMonk.Hunting();
        energy = 10f; // Start with max energy
	}

    private static final float FOCUS_COST = 7f;
    private static final float DASH_COST = 8f;
    private static final float KICK_COST = 6f;
    private static final float MAX_ENERGY = 10f;

    private static final int DASH_RANGE = 4;
    private static final int MIN_DISTANCE_FOR_DASH = 3;
    private static final int KICK_POWER = 4;

    public void addEnergy(float amount){
        energy += amount;
        if (energy > MAX_ENERGY){ energy = MAX_ENERGY; }
    }

    public void spendEnergy(float amount){
        energy -= amount;
        if (energy < 0){ energy = 0; }
    }

    @Override
    public Item createLoot() {
        if (WoodenSpoon.foodEffectAmplifier() != -1 && WoodenSpoon.foodToVariant.containsKey(loot)){
            Class<?> newItemCls = Random.element(WoodenSpoon.foodToVariant.get(loot));
            return (Food) Reflection.newInstance(newItemCls);
        }
        return super.createLoot();
    }

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 25 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 30;
	}
	
	@Override
	public float attackDelay() {
		return super.attackDelay()*0.5f;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}
	
	@Override
	public void rollToDropLoot() {
		Imp.Quest.process( this );
		super.rollToDropLoot();
	}

    private void focus(){
        if (buff(Monk.Focus.class) != null || energy < FOCUS_COST) { return; }
        spendEnergy(FOCUS_COST);
        Buff.affect( this, Monk.Focus.class );
    }

    private void kick(Char enemy){
        //trace a ballistica to our target (which will also extend past them)
        Ballistica trajectory = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
        //trim it to just be the part that goes past them
        trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);

        int oldPos = enemy.pos;
        if (attack(enemy, trajectory.dist > 0 ? 1.5f : 1f , 0, 1f)){
            Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
            if ( oldPos != enemy.pos || !enemy.isActive() || trajectory.dist <= 0 ){ return; }

            WandOfBlastWave.throwChar(enemy, trajectory, KICK_POWER, true, false, Monk.class);
            Buff.affect(enemy, Paralysis.class, 1);
            beckon( enemy.pos );

            if (enemy == Dungeon.hero){
                Dungeon.observe();
                GameScene.updateFog();
            }else{
                enemy.sprite.visible = Dungeon.level.heroFOV[trajectory.collisionPos];
            }

        }
    }


    @Override
    protected boolean doAttack( Char enemy ) {
        if (energy < KICK_COST) { return super.doAttack(enemy); }

        Ballistica ballistica = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
        int charBack = ballistica.path.get(ballistica.dist + 1);
        // Do not kick if the enemy has their back covered
        if (Dungeon.level.solid[charBack] || Actor.findChar(charBack) != null){
            return super.doAttack(enemy);
        }

        spend(TICK);
        spendEnergy(KICK_COST);
        Invisibility.dispel(this);

        if (sprite != null && (sprite.visible || enemy.sprite.visible)) {

            sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase( Messages.get( MonkEnergy.MonkAbility.DragonKick.class, "name" ) ));

            ((StrongMonkSprite) sprite).dragonKick(enemy.pos, new Callback() {
                @Override
                public void call() {
                    kick(enemy);
                    next();
                }
            });
            return false;
        }else{
            kick(enemy);
            return true;
        }
    }

	@Override
	protected boolean act() {
        // Skill priority Kick -> Focus -> Dash
        // Kicks priority over focus comes from (KICK_COST < FOCUS_COST)
        boolean act = super.act();
        focus();
        return act;
	}

	@Override
	protected void spend( float time ) {
        addEnergy(1);
        super.spend( time );
	}

	@Override
	public void move( int step, boolean travelling) {
        // Extra energy if moving, makes kite way harder
        if (travelling) addEnergy(0.67f);
        super.move( step, travelling);
	}

    @Override
	public int defenseSkill( Char enemy ) {
		if (buff(Monk.Focus.class) != null && paralysed == 0 && state != SLEEPING){
			return INFINITE_EVASION;
		}
		return super.defenseSkill( enemy );
	}
	
	@Override
	public String defenseVerb() {
		Monk.Focus f = buff(Monk.Focus.class);
		if (f == null) {
			return super.defenseVerb();
		} else {
			f.detach();
			if (sprite != null && sprite.visible) {
				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			}
			return Messages.get(Monk.class, "parried");
		}
	}

    private void occupyCell(int cell){
        if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) { Door.leave( pos ); }
        pos = cell;
        Dungeon.level.occupyCell(this);
    }

    private class Hunting extends Mob.Hunting{
        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            enemySeen = enemyInFOV;

            if (energy < DASH_COST ||
                    rooted ||
                    !enemyInFOV ||
                    isCharmedBy( enemy ) ||
                    canAttack( enemy ) ||
                    distance(enemy) < MIN_DISTANCE_FOR_DASH)
            {
                return super.act( enemyInFOV, justAlerted );
            }

            Ballistica dash = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);
            if (dash.collisionPos != enemy.pos) return super.act( enemyInFOV, justAlerted );

            int dashPos = -1;
            for (int pathCell : dash.subPath(1, DASH_RANGE)){
                if (Dungeon.level.pit[pathCell]) return super.act( enemyInFOV, justAlerted );

                if (!Dungeon.level.solid[pathCell] && Actor.findChar(pathCell) == null && Dungeon.level.openSpace[pathCell]){
                    dashPos = pathCell;
                }else{
                    break;
                }
            }

            if (dashPos == -1 || Dungeon.level.distance(pos, dashPos) <= 1) return super.act( enemyInFOV, justAlerted );

            spendEnergy(DASH_COST);

            final int dashPosFinal = dashPos;
            if (sprite.visible || Dungeon.hero.fieldOfView[dashPosFinal]){
                Sample.INSTANCE.play(Assets.Sounds.MISS);
                sprite.emitter().start(Speck.factory(Speck.JET), 0.01f, Math.round(4 + 2*Dungeon.level.trueDistance(pos, dashPosFinal)));

                sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase( Messages.get( MonkEnergy.MonkAbility.Dash.class, "name" ) ));

                sprite.jump(pos, dashPosFinal, 0, 0.1f, new Callback() {
                    @Override
                    public void call() {
                        occupyCell(dashPosFinal);
                        if ( enemy == Dungeon.hero ) { Dungeon.hero.interrupt(); }
                        next();
                    }
                });
                spend( TICK );
                return false;
            } else {
                occupyCell(dashPosFinal);
                spend( TICK );
                return true;
            }
        }
    }

    @Override
    public String name() {
        return Messages.get(Monk.class, "name");
    }

    @Override
    public String description() {
        return Messages.get(Monk.class, "desc") + "\n\n" + Messages.get(this, "talent");
    }

    private static final String ENERGY = "energy";

    @Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ENERGY,  energy);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		energy = bundle.getInt( ENERGY );
	}
	
//	public static class Focus extends Buff {
//
//		{
//			type = buffType.POSITIVE;
//			announced = true;
//		}
//
//        public String name() {
//            return Messages.get(Monk.Focus.class, "name");
//        }
//
//        public String desc(){
//            return Messages.get(Monk.Focus.class, "desc");
//        }
//
//		@Override
//		public int icon() {
//			return BuffIndicator.MIND_VISION;
//		}
//
//		@Override
//		public void tintIcon(Image icon) {
//			icon.hardlight(0.25f, 1.5f, 1f);
//		}
//	}
}
