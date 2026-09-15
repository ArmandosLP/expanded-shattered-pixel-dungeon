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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.expanded.ExpandedChallenges;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.StrongSkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class StrongSkeleton extends Skeleton {

	{
		spriteClass = StrongSkeletonSprite.class;
	}

    public enum Weapon {
        NONE,
        SHIELD,
        SPEAR,
        SWORD
    }

    private Weapon weapon = null; //only initially
    private int durability = 0;

    public static void rollForWeapon(Mob m){
        //We roll for a weapon always to ensure that rollForWeapon does not affect levelgen RNG
        float roll = Random.Float();
        if (m instanceof StrongSkeleton && Dungeon.isExpandedChallenged(ExpandedChallenges.STRONGER_MOBS)){
            ((StrongSkeleton) m).durability = 1;
            if (roll < 0.25f){
                ((StrongSkeleton) m).weapon =  Weapon.SWORD;
            } else if (roll < 0.50f){
                ((StrongSkeleton) m).weapon =  Weapon.SPEAR;
            } else if (roll < 0.75f){
                ((StrongSkeleton) m).weapon =  Weapon.SHIELD;
            }else{
                ((StrongSkeleton) m).weapon =  Weapon.NONE;
            }
        }
    }

    private void breakWeapon(){
        breakWeapon(1);
    }

    private void breakWeapon(int dur){
        durability -= dur;

        if (durability <= 0) {
            weapon = Weapon.NONE;

            if (sprite == null) return;
            StrongSkeletonSprite sprite = (StrongSkeletonSprite) this.sprite;

            sprite.setWeapon(StrongSkeleton.Weapon.NONE);
            if (sprite.visible) { sprite.weaponBreakEffect(); }
        }
    }

    @Override
    public CharSprite sprite() {
        if (weapon == null){
            rollForWeapon(this);
        }
        CharSprite sprite = super.sprite();

        if (weapon != null) {
            ((StrongSkeletonSprite)sprite).setWeapon(weapon);
        }
        return sprite;
    }

    @Override
    public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
        boolean result;

        switch (weapon){
            case SWORD:
                result = super.attack(enemy, dmgMulti / 5, dmgBonus, accMulti);
                if (result) {
                    Buff.affect( enemy, Bleeding.class).set( ( (float) damageRoll()) );
                    breakWeapon();
                }
                break;
            case SPEAR:
                result = super.attack(enemy, dmgMulti, dmgBonus, accMulti / 2);
                if (result && distance(enemy) == 1) {
                    breakWeapon();
                }
                break;
            case SHIELD:
            case NONE:
            default:
                result = super.attack(enemy, dmgMulti, dmgBonus, accMulti);
                break;
            }

        return result;
    }

    @Override
    public void damage(int dmg, Object src) {
        super.damage(dmg, src);

        if ( !(src instanceof Blob || src instanceof Buff) && weapon == Weapon.SWORD && isAlive() ){
            breakWeapon(3);
        }
    }

    @Override
	public int damageRoll() {
        if (weapon == Weapon.SPEAR){
            return Random.NormalIntRange( 2, 7 );
        }
        return Random.NormalIntRange( 2, 10 );
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (weapon == Weapon.SHIELD && state == HUNTING && paralysed <= 0){
            return INFINITE_EVASION;
        }
        return super.defenseSkill(enemy);
    }

    @Override
    public String defenseVerb() {
        if (weapon == Weapon.SHIELD && state == HUNTING && paralysed <= 0){
            breakWeapon();
            if (durability > 0 && sprite != null && sprite.visible) {
                Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(1.1f, 1.2f));
            }
            return Messages.get(Monk.class, "parried"); // FIXME maybe different message for shield parry
        }
        return super.defenseVerb();
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        if (weapon == Weapon.SPEAR && enemy != null){

            if (Dungeon.level.distance( pos, enemy.pos ) <= 2){
                boolean[] passable = BArray.not(Dungeon.level.solid, null);

                for (Char ch : Actor.chars()) {
                    //our own tile is always passable
                    passable[ch.pos] = ch == this;
                }

                PathFinder.buildDistanceMap(enemy.pos, passable, 2);

                if (PathFinder.distance[pos] <= 2){ return true; }
            }
        }
        return super.canAttack(enemy);
    }

    @Override
    public String description() {
        String desc = Messages.get(Skeleton.class,"desc");
        switch (weapon){
            case SWORD:
                desc += "\n\n" + Messages.get(this,"sword");
                break;
            case SPEAR:
                desc += "\n\n" + Messages.get(this,"spear");
                break;
            case SHIELD:
                desc += "\n\n" + Messages.get(this,"shield");
                break;
        }
        return desc;
    }

    public static final String WEAPON = "weapon";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(WEAPON, weapon);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        weapon = bundle.getEnum(WEAPON, StrongSkeleton.Weapon.class);
    }

}
