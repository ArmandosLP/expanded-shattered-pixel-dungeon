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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.Frenzy;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class StrongGuard extends Guard {

    {
        HP = HT = 40;
        defenseSkill = 10;

        HUNTING = new StrongGuard.Hunting();
    }


    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(1, 5);
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 1);
    }

    //They have two chains: one for enemy one for Allay
    private boolean chainsUsed = false;
    private boolean allayChainsUsed = false;

    private boolean chain(int target){
        if (chainsUsed || enemy.properties().contains(Property.IMMOVABLE))
            return false;

        Ballistica chain = new Ballistica(pos, target, Ballistica.PROJECTILE);

        if (chain.collisionPos != enemy.pos || chain.path.size() < 2 || Dungeon.level.pit[chain.path.get(1)]) return false;
        else {
            int newPos = -1;
            for (int i : chain.subPath(1, chain.dist)){
                //find the closest position to the guard that's open for the target
                if (!Dungeon.level.solid[i] && Actor.findChar(i) == null && (Dungeon.level.openSpace[i] || !Char.hasProp(enemy, Property.LARGE))){
                    newPos = i;
                    break;
                }
            }

            if (newPos == -1){
                return false;
            } else {
                final int newPosFinal = newPos;
                this.target = newPos;

                if (sprite.visible || enemy.sprite.visible) {
                    yell(Messages.get(this, "scorpion"));
                    new Item().throwSound();
                    Sample.INSTANCE.play(Assets.Sounds.CHAINS);
                    sprite.parent.add(new Chains(sprite.center(),
                            enemy.sprite.destinationCenter(),
                            Effects.Type.CHAIN,
                            new Callback() {
                                public void call() {
                                    Actor.add(new Pushing(enemy, enemy.pos, newPosFinal, new Callback() {
                                        public void call() {
                                            pullMob(enemy, newPosFinal, true);
                                        }
                                    }));
                                    next();
                                }
                            }));
                } else {
                    pullMob(enemy, newPos, true);
                }
            }
        }
        chainsUsed = true;
        return true;
    }

    private void pullMob( Char enemy, int pullPos, boolean cripple){
        enemy.pos = pullPos;
        enemy.sprite.place(pullPos);
        Dungeon.level.occupyCell(enemy);
        if (cripple) { Cripple.prolong(enemy, Cripple.class, 4f); }
        if (enemy == Dungeon.hero) {
            Dungeon.hero.interrupt();
            Dungeon.observe();
            GameScene.updateFog();
        } else {
            enemy.sprite.visible = Dungeon.level.heroFOV[pullPos];
        }
    }

    private ArrayList<Char> availableAllies(){
        ArrayList<Char> accessibleMobs = new ArrayList<>();

        if (enemy == null) return accessibleMobs;

        if (isCharmedBy(Dungeon.hero) || buff(ScrollOfSirensSong.Enthralled.class) != null) {
            accessibleMobs.add(Dungeon.hero);
            return accessibleMobs; // BFF :D
        }

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (fieldOfView[mob.pos] && // Can see
                    (mob.alignment == Alignment.ENEMY) &&
                    !mob.isCharmedBy(enemy) &&
                    !mob.properties().contains(Property.IMMOVABLE) &&
                    !mob.properties().contains(Property.LARGE) &&
                    (mob != this) // Not you, damn it.
            ) { accessibleMobs.add(mob); }
        }

        return accessibleMobs;
    }

    private boolean chainAllay(int target){
        if (allayChainsUsed) return false;

        ArrayList<Char> availableAllies = availableAllies();
        if ( availableAllies.isEmpty() ) return false;

        // chosen an ally if:
        ArrayList<Char> chosenAllies = new ArrayList<>();
        for (Char cha : availableAllies) {
            if (distance(cha) >  6) continue; // Not to far from self
            if (distance(cha) <= 2) continue; // Not to close to self
            if (enemy.distance(this) >= enemy.distance(cha)) continue; // Not closer to enemy then self
            chosenAllies.add(cha);
        }

        if (chosenAllies.isEmpty()){ return false; }

        Char chosenChar = null;
        int newPos = -1;

        for (Char cha : chosenAllies) {
            Ballistica chain = new Ballistica(cha.pos, pos, Ballistica.PROJECTILE);
            if (chain.collisionPos != pos) continue;

            newPos = -1;
            for (int i : chain.subPath(1, chain.dist)) {
                if (!Dungeon.level.solid[i] && Actor.findChar(i) == null && Dungeon.level.openSpace[i]) {
                    newPos = i;
                } else { break; }
            }

            if ( !Dungeon.level.adjacent(pos, newPos) ) { continue; }

            chosenChar = cha;
            break;
        }

        if (chosenChar == null) return false;

        final int newPosFinal = newPos;
        final Char chosenCharFinal = chosenChar;

        if (sprite.visible || enemy.sprite.visible || chosenCharFinal.sprite.visible) {
            yell(Messages.get(this, "attack"));

            new Item().throwSound();
            Sample.INSTANCE.play(Assets.Sounds.CHAINS);

            final Pushing push = new Pushing(chosenCharFinal, chosenCharFinal.pos, newPosFinal, new Callback() {
                public void call() {
                    pullMob(chosenCharFinal, newPosFinal, false);

                    // This allows the guard to "attack" to allay and apply Frenzy
                    Actor actor = new Actor() {
                        { actPriority = VFX_PRIO; }
                        @Override
                        protected boolean act() {
                            final Actor thisActor = this;

                            // Just in case something acted after push and moved or killed the chosen char
                            if (chosenCharFinal.pos != newPosFinal) {
                                thisActor.next();
                                remove(thisActor);
                            }

                            sprite.attack(newPosFinal, new Callback() {
                                @Override
                                public void call() {
                                    // Wakey wakey it's time for School
                                    chosenCharFinal.damage(1,this);
                                    Buff.affect(chosenCharFinal, Frenzy.class).set(1, 0.5f);
                                    SpellSprite.show( chosenCharFinal, SpellSprite.FRENZY);
                                    Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH);
                                    sprite.idle();
                                    thisActor.next();
                                    remove(thisActor);
                                }
                            });
                            return false;
                        }
                    };
                    add(actor);
                }
            });

            sprite.parent.add(new Chains(sprite.center(),
                    chosenCharFinal.sprite.destinationCenter(),
                    Effects.Type.CHAIN,
                    new Callback() {
                        public void call() {
                            allayChainsUsed = true;
                            Actor.add(push);
                            spend(TICK);
                            next(); // Pushing is an actor, next is necessary so push can act
                        }
                    }));
        } else {
            pullMob(chosenCharFinal, newPosFinal, false);
            chosenCharFinal.damage(1,this);
            Buff.affect(chosenCharFinal, Frenzy.class).set(1, 0.5f);
        }
        allayChainsUsed = true;
        return true;
    }

    private class Hunting extends Mob.Hunting{
        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            enemySeen = enemyInFOV;

            if (!chainsUsed && enemyInFOV && !isCharmedBy( enemy ) && !canAttack( enemy ) && Dungeon.level.distance( pos, enemy.pos ) < 5 && chain(enemy.pos)) {
                return !(sprite.visible || enemy.sprite.visible);
            }
            else if (!allayChainsUsed && enemyInFOV && !isCharmedBy( enemy ) && Dungeon.level.distance(pos,enemy.pos) <= 2 && chainAllay(enemy.pos)) {
                if (enemy instanceof Hero){ Dungeon.hero.interrupt(); }
                return !(sprite.visible || enemy.sprite.visible);
            }
            else {
                return super.act( enemyInFOV, justAlerted );
            }

        }
    }

}
