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

package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.npc;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.WanderingImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class WanderingImp extends NPC {

	{
        // Credits for the sprite: @D3aht
		spriteClass = WanderingImpSprite.class;

		properties.add(Property.IMMOVABLE);
	}
	
	private boolean seenBefore = false;

	static final private String SEEN_BEFORE = "SEEN_BEFORE";

	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		seenBefore = bundle.getBoolean( SEEN_BEFORE );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( SEEN_BEFORE, seenBefore );
	}


	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.WANDERING_IMP;
	}

	@Override
	public void destroy() {
		super.destroy();
		for (Heap heap: Dungeon.level.heaps.valueList()) {
			if (heap.type == Heap.Type.FOR_SOUL_SALE) {
				if (heap.size() == 1) {
					heap.destroy();
				} else {
					heap.items.remove(heap.size()-1);
					heap.type = Heap.Type.HEAP;
				}
			}
		}
	}

	@Override
	protected boolean act() {
		if (Dungeon.levelHasBeenGenerated(Dungeon.depth + 1, Dungeon.branch)) {
			destroy();
			sprite.die();
			return super.act();
		}

		if (!seenBefore && Dungeon.level.heroFOV[pos]) {
			yell(Messages.get(this, "yell", Messages.titleCase(Dungeon.hero.name())));
			seenBefore = true;
            sprite.turnTo( pos, Dungeon.hero.pos );
		}

		spend(TICK);
		return super.act();

	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(Char c) {
		sprite.turnTo( pos, Dungeon.hero.pos );
        tell(Messages.get(this,"interact"));
		return true;
	}
	
	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( WanderingImp.this, text ));
			}
		});
	}
}
