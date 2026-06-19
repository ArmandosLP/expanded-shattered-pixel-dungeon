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

package com.shatteredpixel.shatteredpixeldungeon.expanded.rooms;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.npc.WanderingImp;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SoulPactRoom extends SpecialRoom {

	protected ArrayList<Item> itemsToSpawn;
	static private final int MAX_PACT_AMOUNT = 7;
	static public final int MIN_PACT_AMOUNT = 4;
    static public final int REGULAR_CHANCE_INCREASE = 2;
    static public final int BOSS_CHANCE_INCREASE = 4;
    static public final int CHANCE_REDUCTION = 100;

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

		Mob imp = new WanderingImp();
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
        // We generate all the possible prizes and randomly take 4 of them.
        ArrayList<Item> possiblePrizes = new ArrayList<>();

        possiblePrizes.add(Generator.random(Generator.Category.WAND));
        possiblePrizes.add(Generator.random(Generator.Category.RING));
        possiblePrizes.add(Generator.random(Generator.Category.WEAPON));
        possiblePrizes.add(Generator.random(Generator.Category.ARMOR));
        possiblePrizes.add(Generator.random(Generator.Category.MISSILE));
        possiblePrizes.add(new ScrollOfEnchantment());
        possiblePrizes.add(new PotionOfMastery());

        ArrayList<Item> itemsToSpawn = new ArrayList<>();

        for(int i = 0; i < MIN_PACT_AMOUNT; i++){
            if (possiblePrizes.isEmpty() || itemsToSpawn.size() >= MAX_PACT_AMOUNT) break;

            Item prize = Random.element(possiblePrizes);
            possiblePrizes.remove(prize);
            itemsToSpawn.add(prize);

            if (prize.isUpgradable()){
                int level = 1; // +1: 100%
                level += Random.Int(2); // Extra +1: 50%

                while (Random.Int(5) == 0){
                    level += 1; // Theoretically infinite upgrades, chances of getting a +5 item are 1 in 250
                }
                prize.level(level);
            }
        }
        return itemsToSpawn;
	}

    public static SoulPactRoom pactForFloor(int depth){
        if (depth <= 4){
            // 1% chance for pact in the sewers
            if (Random.Int(100) == 0) return new SoulPactRoom();
            return null;
        }else{
            int pactChance = Dungeon.LimitedDrops.BLOOD_PACT_CHANCE.count;

            if (Random.Int(100) + 1 <= pactChance){
                addPactChance(-CHANCE_REDUCTION);
                return new SoulPactRoom();
            }
            return null;
        }
    }

    public static void addPactChance(int value){
        int pactChance = Dungeon.LimitedDrops.BLOOD_PACT_CHANCE.count;
        pactChance = Math.max(0, pactChance + value);
        Dungeon.LimitedDrops.BLOOD_PACT_CHANCE.count = pactChance;
    }

}
