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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SpectralWallParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class StrongGolem extends Golem {

    {
        HUNTING = new StrongGolem.Hunting();
    }

    private int doorLockCooldown = 0;

    private static final String DOOR_LOCK_COOLDOWN = "door_lock_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
        bundle.put(DOOR_LOCK_COOLDOWN, doorLockCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
        doorLockCooldown = bundle.getInt( DOOR_LOCK_COOLDOWN );
	}

	@Override
	protected boolean act() {
        doorLockCooldown--;
        return super.act();
	}

    private boolean canLock(int target){
        if (doorLockCooldown > 0) return false;

        PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(pos, target)+1);
        //zaps can go around blocking terrain, but not through it
        if (PathFinder.distance[pos] == Integer.MAX_VALUE){
            return false;
        }

        int door = -1;
        for (int i : PathFinder.NEIGHBOURS8){
            int p = target + i;
            if (
                    Actor.findChar( p ) == null &&
                    (Dungeon.level.map[p] == Terrain.DOOR || Dungeon.level.map[p] == Terrain.OPEN_DOOR) &&
                    (Dungeon.level.blobs.get(GolemWall.class) == null || Dungeon.level.blobs.get(GolemWall.class).cur[p] <= 0)
            ) {
                door = p;
                break;
            }
        }

        return door != -1;
    }

    private void lockDoor(int doorPos){
        Level.set( doorPos, Terrain.OPEN_DOOR );
        GameScene.updateMap( doorPos );

        Blob wall = Dungeon.level.blobs.get(GolemWall.class);
        if (!Dungeon.level.solid[doorPos] || (wall != null && wall.cur[doorPos] > 0)) {
            GameScene.add(Blob.seed(doorPos, 20, GolemWall.class));
        }

        if (Dungeon.level.heroFOV[doorPos]) {
            Dungeon.observe();
            Sample.INSTANCE.play( Assets.Sounds.OPEN );
        }

        if (enemy instanceof Hero){
            ((Hero) enemy).interrupt();
            Dungeon.observe();
            GameScene.updateFog();
        }
    }

    private boolean doLockDoor(){
        spend(TICK);

        int doorPos = -1;
        for (int i : PathFinder.NEIGHBOURS8){
            int p = target + i;
            if (
                    Actor.findChar( p ) == null &&
                    (Dungeon.level.map[p] == Terrain.DOOR || Dungeon.level.map[p] == Terrain.OPEN_DOOR) &&
                    (Dungeon.level.blobs.get(GolemWall.class) == null || Dungeon.level.blobs.get(GolemWall.class).cur[p] <= 0)
            ) {
                doorPos = p;
                break;
            }
        }

        doorLockCooldown = 20;

        if (sprite != null && (Dungeon.hero.fieldOfView[target] || enemy.sprite.visible)) {
            final int finalDoorPos = doorPos;

            Sample.INSTANCE.play( Assets.Sounds.ZAP );
            sprite.zap(doorPos, new Callback() { @Override public void call() {} });

            MagicMissile.boltFromChar( sprite.parent,
                    MagicMissile.ELMO,
                    sprite,
                    doorPos,
                    new Callback() {
                        @Override
                        public void call() {
                            GLog.w(Messages.get(StrongGolem.class, "doorlock"));
                            Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
                            lockDoor(finalDoorPos);
                            next();
                        }
                    } );

            return false;
        }else{
            lockDoor(doorPos);
            return true;
        }
    }

    @Override
    public String description() {
        return Messages.get(Golem.class, "desc") + "\n\n" + Messages.get(this, "talent");
    }

	private class Hunting extends Mob.Hunting{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV || canAttack(enemy)) {
				return super.act(enemyInFOV, justAlerted);
			} else {

				if (handleRecentAttackers()){
					return act( true, justAlerted );
				}

				enemySeen = true;
				target = enemy.pos;

				int oldPos = pos;

				if (distance(enemy) >= 1 && Random.Int(100/distance(enemy)) == 0
						&& !Char.hasProp(enemy, Property.IMMOVABLE) && canTele(target)){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						teleportEnemy();
						return true;
					}
                } else if (canLock(target)){
                    return doLockDoor();
                }else if (getCloser( target )) {
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );
				} else if (!Char.hasProp(enemy, Property.IMMOVABLE) && canTele(target)) {
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						teleportEnemy();
						return true;
					}
				} else {
					//attempt to swap targets if the current one can't be reached or teleported
					return handleUnreachableTarget(enemyInFOV, justAlerted);
				}

			}
		}
	}

    public static class GolemWall extends Blob {

        {
            alwaysVisible = true;
        }

        @Override
        protected void evolve() {
            int cell;
            boolean cellEnded = false;

            Level l = Dungeon.level;
            for (int i = area.left; i < area.right; i++){
                for (int j = area.top; j < area.bottom; j++){
                    cell = i + j*l.width();
                    off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;

                    if (cur[cell] > 0 && off[cell] == 0){
                        cellEnded = true;
                    }

                    //caps at 10 turns
                    off[cell] = Math.min(off[cell], 9);

                    volume += off[cell];

                    if (off[cell] == 0 && cur[cell] > 0){
                        cellsToFlagUpdate.add(cell);
                    }
                }
            }

            if (cellEnded){
                Dungeon.observe();
            }
        }

        @Override
        public void seed(Level level, int cell, int amount) {
            super.seed(level, cell, amount);
            level.updateCellFlags(cell);
        }

        @Override
        public void clear(int cell) {
            super.clear(cell);
            if (cur == null) return;
            Dungeon.level.updateCellFlags(cell);
        }

        @Override
        public void fullyClear() {
            super.fullyClear();
            Dungeon.level.buildFlagMaps();
        }

        @Override
        public void onBuildFlagMaps(Level l) {
            if (volume > 0){
                for (int i=0; i < l.length(); i++) {
                    onUpdateCellFlags(l, i);
                }
            }
        }

        @Override
        public void onUpdateCellFlags(Level l, int cell) {
            if (volume > 0 && cur[cell] > 0) {
                l.losBlocking[cell] =  true;
                l.solid[cell] = true;
                l.passable[cell] = false;
                l.avoid[cell] = false;
                //openSpace will be updated as part of updating flags in Level
            }
        }

        @Override
        public void use(BlobEmitter emitter) {
            super.use( emitter );
            emitter.pour(SpectralWallParticle.FACTORY, 0.05f );
        }

        @Override
        public String tileDesc() {
            return Messages.get(this, "desc");
        }

    }

}
