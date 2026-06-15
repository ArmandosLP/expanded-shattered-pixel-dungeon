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

package com.shatteredpixel.shatteredpixeldungeon.expanded;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class BloodPactRoom extends SpecialRoom {

	protected ArrayList<Item> itemsToSpawn;
	static private int MAX_PACT_AMOUNT = 8;
	static public int MIN_PACT_AMOUNT = 4;

	@Override
	public int minWidth() {
		return 7;
	}
	
	@Override
	public int minHeight() {
		return 7;
	}

	@Override
	public int maxWidth() {
		return 7;
	}

	@Override
	public int maxHeight() {
		return 7;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}
	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	public void paint( Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		Painter.fill( level, this, 2, Terrain.EMPTY_SP );

		placeShopkeeper( level );

		placeItems( level );
		
		for (Door door : connected.values()) {
			door.set( Door.Type.LOCKED );
		}

	}

	protected void placeShopkeeper( Level level ) {

		int pos = level.pointToCell(center());

		Mob imp = new BloodPactImp();
		imp.pos = pos;
		level.mobs.add( imp );

	}

	protected void placeItems( Level level ){

		if (itemsToSpawn == null){
			itemsToSpawn = generateItems();
		}

		int[][] directions = {
				{0, 1},
				{0, -1},
				{1, 0},
				{-1, 0},
				{-1, -1},
				{1, 1},
				{-1, 1},
				{1, -1}
		};

		int inset = 0;

		for (Item item : itemsToSpawn.toArray(new Item[0])) {
			Point centerEntryInset = new Point(center());

			int[] offset = directions[inset];

			centerEntryInset.x += offset[0];
			centerEntryInset.y += offset[1];

			int cell = level.pointToCell(centerEntryInset);

			level.drop( item, cell ).type = Heap.Type.FOR_SOUL_SALE;
			itemsToSpawn.remove(item);
			inset += 1;
		}

		if (!itemsToSpawn.isEmpty()){
			ShatteredPixelDungeon.reportException(new RuntimeException("failed to place all items in a shop!"));
		}
	}
	
	protected static ArrayList<Item> generateItems() {
		ArrayList<Item> itemsToSpawn = new ArrayList<>();

		for (int i = 0; i < MIN_PACT_AMOUNT; i++){
			if (i >= MAX_PACT_AMOUNT) break;

			Item prize;

			switch (Random.chances(new float[]{2, 2, 2, 2, 2, 1, 1, 2, 2})){
				default:
				case 0:
					prize = Generator.random(Generator.Category.WAND); break;
				case 1:
					prize = Generator.random(Generator.Category.RING); break;
				case 2:
					prize = Generator.random(Generator.Category.WEAPON); break;
				case 3:
					prize = Generator.random(Generator.Category.ARMOR); break;
				case 4:
					prize = Generator.random(Generator.Category.MISSILE); break;
				case 5:
					prize = new ScrollOfUpgrade(); break;
				case 6:
					prize =  new PotionOfStrength(); break;
				case 7:
					prize =  new PotionOfExperience(); break;
				case 8:
					prize =  new ScrollOfTransmutation(); break;
			}

			if (!(prize instanceof Potion) && !(prize instanceof Scroll)){
				switch (Random.chances(new float[]{1, 1, 1})){
					case 0:
						break;
					case 1:
						prize.level(prize.level() + 1);
						break;
					case 2:
						prize.level(prize.level() + 2);
						break;
				}

				prize.levelKnown = true;

			}

			itemsToSpawn.add(prize);
		}

		return itemsToSpawn;

	}

}
