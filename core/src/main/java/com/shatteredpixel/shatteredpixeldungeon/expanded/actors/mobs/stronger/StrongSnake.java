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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class StrongSnake extends Snake {

    // If ExpandedChallenges.STRONGER_MOBS is active, snakes can see through tall/furrowed grass and do not destroy it when walking on it.
    // See Level.updateFieldOfView() and HighGrass.trample()
    // FIXME It would be nice to have a buff that allows seeing through high grass instead of having to change trample() for every mob


    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }
}
