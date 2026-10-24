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

package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.GoldenSkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GoldenSkeleton extends Skeleton {
	
	{
		spriteClass = GoldenSkeletonSprite.class;
		
		HP = HT = 100;
		defenseSkill = 0;

		loot = null;
		lootChance = 1f;
		baseSpeed = 0.5f;

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}

    @Override
    public float attackDelay() {
        return super.attackDelay() * 2f;
    }

    @Override
	public int damageRoll() {
		return Random.NormalIntRange( 2, 8 );
	}

	@Override
    public void die( Object cause ) {
        // super checks if this is instance of GoldenSkeleton to skip bone explosion
        super.die( cause );
    }

	@Override
	public int drRoll() {
        return super.drRoll() + 5;
	}

    @Override
    public void rollToDropLoot() {
        super.rollToDropLoot();
        if (Dungeon.hero.lvl > maxLvl + 2) return;

        for (int ofs : PathFinder.NEIGHBOURS8){
            int dropCell = pos + ofs;
            dropCell = Dungeon.level.solid[dropCell] || Dungeon.level.avoid[dropCell] ? pos : dropCell;
            Dungeon.level.drop( new Gold(Random.IntRange(25, 50), true), dropCell).sprite.drop( pos );
        }

    }

    @Override
    public void damage(int dmg, Object src) {
        super.damage(dmg / 2, src); // half damage, even for magic and buffs
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (sprite != null && sprite.visible) {
            Sample.INSTANCE.play(Assets.Sounds.GOLD, 1f, Random.Float(0.87f, 1.15f) * 1.2f);
            Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 0.3f, Random.Float(0.87f, 1.15f));
        }
        return super.defenseProc(enemy, damage);
    }

    public void sell(){
        if (Dungeon.hero.lvl <= maxLvl + 2) {
            Dungeon.level.drop( new Gold(400, true), pos).sprite.drop( pos );
        }

        if (sprite != null && sprite.visible) {
            Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
            CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
            GLog.i(Messages.get(this, "sell"));
        }else{
            GLog.i( Messages.get(this, "died") );
        }

        destroy();
        sprite.killAndErase();
    }

}
