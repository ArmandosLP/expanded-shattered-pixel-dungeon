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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class StrongCrab extends Mob {

	{
		spriteClass = CrabSprite.class;
		
		HP = HT = 15;
		defenseSkill = 5;
		baseSpeed = 2f;
		
		EXP = 4;
		maxLvl = 9;
		
		loot = MysteryMeat.class;
		lootChance = 0.167f;

	}

    private boolean canSwap(Char ch){
        if (ch instanceof StrongCrab){ return false; }

        if (ch.invisible > 0) { return false; }
        if (ch.properties().contains(Property.IMMOVABLE) || ch.properties().contains(Property.LARGE)) { return false; }
        if (rooted || ch.rooted){ return false; }

        if (isCharmedBy(Dungeon.hero) || buff(ScrollOfSirensSong.Enthralled.class) != null){
            if (ch.alignment != Alignment.ALLY ) { return false; }
        }else{
            if (ch.alignment != Alignment.ENEMY ) { return false; }
        }

        if (Dungeon.level.distance(ch.pos, target) >= Dungeon.level.distance(pos, target)){ return false; }
        return canInteract(ch);
    }

    @Override
    protected boolean getCloser(int target) {
        boolean result = super.getCloser(target);
        if (state != HUNTING){ return result; }

        Char swapChar = null;

        if (!result){
            for (int n : PathFinder.NEIGHBOURS8){
                Char neighbourChar = Actor.findChar(pos+n);
                if (neighbourChar != null && canSwap(neighbourChar)){
                    swapChar = neighbourChar;
                    break;
                }
            }

            if (swapChar != null){
                Actor.add(new Swapper(this, swapChar));
                return true;
            }
        }

        return result;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 1, 7 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 12;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }


    @Override
    public String name() {
        return Messages.get(Crab.class, "name");
    }

    @Override
    public String description() {
        return Messages.get(Crab.class, "desc") + "\n\n" + Messages.get(this, "talent");
    }

    public static class Swapper extends Actor {
        { actPriority = VFX_PRIO; }

        Char swaperChar;
        Char swapedChar;

        public Swapper(Char swaperChar, Char swapedChar) {
            this.swaperChar = swaperChar;
            this.swapedChar = swapedChar;
        }

        @Override
        protected boolean act() {
            if (!swaperChar.isAlive() || !swapedChar.isAlive()) {
                remove(this);
                return true;
            }

            if (swaperChar.sprite.visible ||  swapedChar.sprite.visible) {
                swaperChar.sprite.attack(swapedChar.pos, new Callback() {
                    @Override
                    public void call() {
                        swaperChar.hitSound(1);
                        swapedChar.sprite.flash();
                        swapChars();
                    }
                });
                return false;
            }else{
                swapChars();
                return true;
            }
        }

        private void swapChars(){
            final int swapedPos = swapedChar.pos; // swaped can die with 1 damage, avoid null pointer
            swapedChar.damage(1,this);

            if (swapedChar != null && swapedChar.isAlive()){
                swaperChar.interact(swapedChar);
            } else {
                swaperChar.move(swapedPos);
            }

            next();
            remove(this);
        }

    }

}
