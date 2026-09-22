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
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

public class ExpandedChallenges {
    public static final int STRONGER_MOBS		= 1;
//	public static final int NO_ARMOR			= 2;
//	public static final int NO_HEALING			= 4;
//	public static final int NO_HERBALISM		= 8;

	public static final int MAX_VALUE           = 1;
	public static final int MAX_CHALS           = 1;

	public static final String[] NAME_IDS = {
			"stronger_mobs",
	};

	public static final int[] MASKS = {
            STRONGER_MOBS
	};

	public static int activeChallenges(){
		return activeChallenges(Dungeon.expandedChallenges);
	}

	public static int activeChallenges(int mask){
		int chCount = 0;
		for (int ch : ExpandedChallenges.MASKS){
			if ((mask & ch) != 0) chCount++;
		}
		return chCount;
	}

}