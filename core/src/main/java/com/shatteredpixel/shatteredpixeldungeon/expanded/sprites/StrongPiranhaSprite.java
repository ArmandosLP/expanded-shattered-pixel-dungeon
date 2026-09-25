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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;

public class StrongPiranhaSprite extends MobSprite {

    private Animation suffocate;

	public StrongPiranhaSprite() {
		super();

        renderShadow = false;
        perspectiveRaise = 0.2f;

		texture( Assets.Sprites.STRONG_PIRANHA );
		
		TextureFilm frames = new TextureFilm( texture, 12, 16 );
		
		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1, 2, 1 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0, 1, 2, 1 );
		
		attack = new Animation( 20, false );
		attack.frames( frames, 3, 4, 5, 6, 7, 8, 9, 10, 11 );
		
		die = new Animation( 4, false );
		die.frames( frames, 12, 13, 14 );

        suffocate = new Animation( 25, true );
        suffocate.frames( frames, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);

        play( idle );
	}

    private static int sufF = 15;

    @Override
    public void update() {
        super.update();
        if (onLand){
            if (curAnim == suffocate){
                float f = sufF - curFrame;

                if (f >= (double) (sufF / 2)){
                    f = sufF - f;
                }

                shadowWidth  = 1f - (0.04f * f);
                shadowOffset = 0.5f + (0.25f * f);
            }

            renderShadow = true;
        }else{
            renderShadow = false;
            perspectiveRaise = 0.2f;
        }
    }

    private boolean onLand = false;

    public void onLand(boolean onLand){
        this.onLand = onLand;
        if (onLand){
            play(suffocate);
        }else{
            play(idle);
        }
    }

	@Override
	public void link(Char ch) {
		super.link(ch);
		renderShadow = false;
	}

	@Override
	public void onComplete( Animation anim ) {
		super.onComplete( anim );

		if (anim == suffocate && ch.isAlive()){
            GameScene.ripple( ch.pos );
        }

		if (anim == attack) {
			GameScene.ripple( ch.pos );
		}
	}

}
