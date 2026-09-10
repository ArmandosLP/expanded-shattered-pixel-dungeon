package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger.StrongSuccubus;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.IncubusSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;

import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Incubus extends Mob {

    public int succubusID = -1;

    {
        spriteClass = IncubusSprite.class;

        HP = HT = 80;
        defenseSkill = 25;

        viewDistance = Light.DISTANCE;

        EXP = 12;
        maxLvl = 25;

        lootChance = 1f;

        properties.add(Property.DEMONIC);

        // Incubus is neutral when invisible
        alignment = Alignment.ENEMY;

        WANDERING = new Wandering();
        HUNTING = new Hunting();
    }

    private class Wandering extends Mob.Wandering {

        @Override
        protected boolean continueWandering() {
            enemySeen = false;

            StrongSuccubus succubus = (StrongSuccubus) Actor.findById( succubusID );
            if (succubus != null && (succubus.state != succubus.WANDERING || Dungeon.level.distance( pos,  succubus.getTarget()) > 1)){
                target = succubus.pos;
                int oldPos = pos;
                if (getCloser( target )){
                    spend( 1 / speed() );
                    return moveSprite( oldPos, pos );
                } else {
                    spend( TICK );
                    return true;
                }
            } else {
                return super.continueWandering();

            }
        }
    }

    @Override
    public int defenseProc( Char enemy, int damage ) {
        return super.defenseProc(enemy, damage);
    }

    @Override
    public int damageRoll() {
        return 1;
//      return Random.NormalIntRange(25, 30);
    }

    @Override
    public float speed() {
        // Moves faster when not invisible to be able to escape
        return super.speed();
//        return super.speed() * (invisible() ? 1f : 2f);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (true) { Buff.prolong(enemy, Cripple.class, Cripple.DURATION); }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void damage(int dmg, Object src) {
        // no damage
        super.damage(dmg, src);
    }

    @Override
    protected Char chooseEnemy() {
        return null;
//        if (invisible() && state != SLEEPING && Dungeon.hero != null && Dungeon.hero.isAlive()) {
//            return Dungeon.hero;
//        }
//        return super.chooseEnemy();
    }

    @Override
    protected boolean act() {
        boolean result = super.act();
        return result;
    }

    protected class Hunting extends Mob.Hunting {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            return super.act(enemyInFOV, justAlerted);
        }
    }

}

