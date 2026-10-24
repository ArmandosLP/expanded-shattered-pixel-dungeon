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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.Ferocity;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongRipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class StrongRipperDemon extends RipperDemon {

	{
		spriteClass = StrongRipperSprite.class;
        HUNTING = new StrongRipperDemon.Hunting();
    }

    @Override
    public String description() {
        return Messages.get(RipperDemon.class, "desc") + "\n\n" + Messages.get(this, "talent");
    }

    @Override
    protected boolean act() {
        if (paralysed <= 0) screechCooldown--;
        return super.act();
    }

    private float screechCooldown = 0;

    public void reduceScreechCooldown(int amount){
        // Do not reduce screech cooldown below 2 to avoid rippers spamming it
        screechCooldown = Math.max(screechCooldown - amount, 2);
    }

    private void screech(){
        for (Mob mob : Dungeon.level.mobs){
            if (mob.getClass() == StrongRipperDemon.class && mob != this){
                StrongRipperDemon ripper = (StrongRipperDemon) mob;
                if (fieldOfView[ripper.pos]){
                    SpellSprite.show( ripper, SpellSprite.FEROCITY);
                    reduceScreechCooldown(3);
                    Buff.affect(ripper, Ferocity.class).set(5, true);
                }else if (!ripper.isTargeting(enemy)){
                    ripper.beckon( enemy.pos );
                }
            }
        }
    }

    public boolean doScreech(Char enemy){
        if (sprite.visible){
            Dungeon.hero.interrupt();
            GLog.w(Messages.get(this, "screech"));

            ((StrongRipperSprite) sprite).screech(enemy.pos, new Callback() {
                @Override
                public void call() {
                    screech();
                    next();
                }
            });

            return false;
        }else{
            screech();
            return true;
        }
    }

    public class Hunting extends Mob.Hunting {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {

            if (leapPos != -1){

                leapCooldown = Random.NormalIntRange(2, 4);

                if (rooted){
                    leapPos = -1;
                    return true;
                }

                Ballistica b = new Ballistica(pos, leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                leapPos = b.collisionPos;

                final Char leapVictim = Actor.findChar(leapPos);
                final int endPos;

                //ensure there is somewhere to land after leaping
                if (leapVictim != null){
                    int bouncepos = -1;
                    //attempt to bounce in free passable space
                    for (int i : PathFinder.NEIGHBOURS8){
                        if ((bouncepos == -1 || Dungeon.level.trueDistance(pos, leapPos+i) < Dungeon.level.trueDistance(pos, bouncepos))
                                && Actor.findChar(leapPos+i) == null && Dungeon.level.passable[leapPos+i]){
                            bouncepos = leapPos+i;
                        }
                    }
                    //try again, allowing a bounce into any non-solid terrain
                    if (bouncepos == -1){
                        for (int i : PathFinder.NEIGHBOURS8){
                            if ((bouncepos == -1 || Dungeon.level.trueDistance(pos, leapPos+i) < Dungeon.level.trueDistance(pos, bouncepos))
                                    && Actor.findChar(leapPos+i) == null && !Dungeon.level.solid[leapPos+i]){
                                bouncepos = leapPos+i;
                            }
                        }
                    }
                    //if no valid position, cancel the leap
                    if (bouncepos == -1) {
                        leapPos = -1;
                        return true;
                    } else {
                        endPos = bouncepos;
                    }
                } else {
                    endPos = leapPos;
                }

                //do leap
                sprite.visible = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos] || Dungeon.level.heroFOV[endPos];
                sprite.jump(pos, leapPos, new Callback() {
                    @Override
                    public void call() {

                        if (leapVictim != null && alignment != leapVictim.alignment){
                            if (hit(StrongRipperDemon.this, leapVictim, Char.INFINITE_ACCURACY, false)) {
                                Buff.affect(leapVictim, Bleeding.class).set(0.75f * damageRoll());
                                leapVictim.sprite.flash();
                                Sample.INSTANCE.play(Assets.Sounds.HIT);
                            } else {
                                leapVictim.sprite.showStatus( CharSprite.NEUTRAL, leapVictim.defenseVerb() );
                                Sample.INSTANCE.play(Assets.Sounds.MISS);
                            }
                        }

                        if (endPos != leapPos){
                            Actor.add(new Pushing(StrongRipperDemon.this, leapPos, endPos));
                        }

                        pos = endPos;
                        leapPos = -1;
                        sprite.idle();
                        Dungeon.level.occupyCell(StrongRipperDemon.this);
                        next();
                    }
                });
                return false;
            }

            enemySeen = enemyInFOV;

            if ( screechCooldown <= 0 && enemyInFOV && !isCharmedBy( enemy ) && !canAttack( enemy ) ){
                screechCooldown = 10;
                return doScreech(enemy);
            }

            if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

                recentlyAttackedBy.clear();
                target = enemy.pos;
                return doAttack( enemy );

            } else {

                if (enemyInFOV) {
                    target = enemy.pos;
                } else if (enemy == null) {
                    state = WANDERING;
                    target = Dungeon.level.randomDestination( StrongRipperDemon.this );
                    return true;
                }

                if (leapCooldown <= 0 && enemyInFOV && !rooted
                        && Dungeon.level.distance(pos, enemy.pos) >= 3) {

                    int targetPos = enemy.pos;
                    if (lastEnemyPos != enemy.pos){
                        int closestIdx = 0;
                        for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
                            if (Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[i])
                                    < Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[closestIdx])){
                                closestIdx = i;
                            }
                        }
                        targetPos = enemy.pos + PathFinder.CIRCLE8[(closestIdx+4)%8];
                    }

                    Ballistica b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    //try aiming directly at hero if aiming near them doesn't work
                    if (b.collisionPos != targetPos && targetPos != enemy.pos){
                        targetPos = enemy.pos;
                        b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    }
                    if (b.collisionPos == targetPos){
                        //get ready to leap
                        leapPos = targetPos;
                        //don't want to overly punish players with slow move or attack speed
                        float delay = GameMath.gate(attackDelay(), (int)Math.ceil(enemy.cooldown()), 3*attackDelay());
                        spend(delay);
                        if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos]){
                            GLog.w(Messages.get(StrongRipperDemon.this, "leap"));
                            GameScene.targetedCell(leapPos, delay);
                            ((StrongRipperSprite)sprite).leapPrep( leapPos );
                            Dungeon.hero.interrupt();
                        }
                        return true;
                    }
                }

                int oldPos = pos;
                if (target != -1 && getCloser( target )) {

                    spend( 1 / speed() );
                    return moveSprite( oldPos,  pos );

                } else {
                    spend( TICK );
                    if (!enemyInFOV) {
                        sprite.showLost();
                        state = WANDERING;
                        target = Dungeon.level.randomDestination( StrongRipperDemon.this );
                    }
                    return true;
                }
            }
        }

    }

    private static final String SCREECH_CD = "screech_cd";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SCREECH_CD, screechCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        screechCooldown = bundle.getFloat(SCREECH_CD);
    }
}