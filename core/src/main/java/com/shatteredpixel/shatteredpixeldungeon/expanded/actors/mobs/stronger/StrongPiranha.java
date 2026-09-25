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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class StrongPiranha extends Piranha {

	{
		spriteClass = StrongPiranhaSprite.class;

//        WANDERING = new StrongPiranha.Wandering();
	}

    private boolean onLand = false;

	@Override
	protected boolean act() {
        if ((Dungeon.level.water[pos] && !flying) && onLand){
            onLand(false);
            return super.act();
        }

        if (super.act()){
            damage(Math.min(HT/10, HP), Dungeon.hero);
            Buff.affect(this, Vertigo.class,2);
            spend(TICK);
            return true;
        }else {
            return false;
        }
	}

//    private class Wandering extends Mob.Wandering{
//        @Override
//        public boolean act(boolean enemyInFOV, boolean justAlerted) {
//            if (enemyInFOV) {
//                PathFinder.buildDistanceMap(enemy.pos, Dungeon.level.water, viewDistance);
//                enemyInFOV = PathFinder.distance[pos] != Integer.MAX_VALUE;
//            }
//
//            return super.act(enemyInFOV, justAlerted);
//        }
//    }

    private int randomCell(){
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS8) {
            int cell = pos + n;
            if (!Dungeon.level.solid[cell] && Actor.findChar( cell ) == null
                    && (!hasProp(this,Property.LARGE) || Dungeon.level.openSpace[cell])) {
                candidates.add( cell );
            }
        }

        return !candidates.isEmpty() ? Random.element(candidates) : -1;
    }

    @Override
    protected boolean getCloser( int target ) {
        if (rooted || !onLand) {
            return super.getCloser(target);
        }

        int step = Dungeon.findStep( this, target, Dungeon.level.passable, fieldOfView, true );

        if (step != -1) {
            move( step );
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean getFurther( int target ) {
        if (rooted || !onLand) {
            return super.getFurther(target);
        }

        int step = Dungeon.flee( this, target, Dungeon.level.passable, fieldOfView, true );

        if (step != -1) {
            move( step );
            return true;
        } else {
            return false;
        }
    }


    private void doRandomMove(){
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS8) {
            int cell = pos + n;
            if (!Dungeon.level.solid[cell] && Actor.findChar( cell ) == null
                    && (!hasProp(this,Property.LARGE) || Dungeon.level.openSpace[cell])) {
                candidates.add( cell );
            }
        }

        if (candidates.isEmpty()){
            return;
        }

        int newPos = Random.element(candidates);

        if (sprite != null && sprite.visible || (Dungeon.hero.fieldOfView != null && Dungeon.hero.fieldOfView[newPos])){
            Pushing push = new Pushing(this, pos, newPos, new Callback() {
                @Override
                public void call() {
                    randomMove(newPos);
                    next();
                }
            });
            add(push);
            return;
        }else{
            randomMove(newPos);
            return;
        }
    }

    private void randomMove(int newPos){
        pos = newPos;
        sprite.place(newPos);
        Dungeon.level.occupyCell( this );

        if (Dungeon.level.pit[newPos]){
            die(Chasm.class);
        }else if (Dungeon.level.water[newPos]){
            onLand(false);
        }
    }

	private void onLand(boolean state){
        onLand = state;
        ((StrongPiranhaSprite) sprite).onLand(state);

        if (onLand){
            for (Class c : new BlobImmunity().immunities()){
                if (c != Electricity.class && c != Freezing.class){
                    immunities.remove(c);
                }
            }
            immunities.remove( Burning.class );
            spend(cooldown() + TICK);
        }else{
            for (Class c : new BlobImmunity().immunities()){
                if (c != Electricity.class && c != Freezing.class){
                    immunities.add(c);
                }
            }
            immunities.add( Burning.class );
        }

	}

    @Override
    public CharSprite sprite() {
        StrongPiranhaSprite sprite = (StrongPiranhaSprite)super.sprite();
        sprite.onLand(onLand);
        return sprite;
    }

    @Override
    public void dieOnLand(){
        // We leave this method because it is used outside Piranha
        if (!onLand){
            onLand(true);
        }
    }

    public static final String ON_LAND = "on_land";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ON_LAND,onLand);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        onLand = bundle.getBoolean(ON_LAND);
    }
}
