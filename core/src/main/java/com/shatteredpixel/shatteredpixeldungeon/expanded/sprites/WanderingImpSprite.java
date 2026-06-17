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

package com.shatteredpixel.shatteredpixeldungeon.expanded.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class WanderingImpSprite extends MobSprite {

	public WanderingImpSprite() {
		super();
		
		texture( Assets.Sprites.IMP );
		
		TextureFilm frames = new TextureFilm( texture, 12, 14 );
        int c = 5;

		idle = new Animation( 5, true );
		idle.frames( frames,
                c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+1,c+1,c+1,
                        c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+2,c+2);
		
		run = new Animation( 20, true );
		run.frames( frames, c+0 );
		
		die = new Animation( 10, false );
		die.frames( frames, c+0,c+0,c+0,c+0,c+0,c+0,c+0,c+1,c+1);
		
		play( idle );
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == die) {
			
			emitter().burst( Speck.factory( Speck.WOOL ), 15 );
			killAndErase();
			
		} else {
			super.onComplete( anim );
		}
	}



}
