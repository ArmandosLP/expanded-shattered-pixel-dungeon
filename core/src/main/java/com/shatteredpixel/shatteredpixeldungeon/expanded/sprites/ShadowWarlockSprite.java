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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

public class ShadowWarlockSprite extends MobSprite {

    private Emitter particles;

	public ShadowWarlockSprite() {
		super();
		
		texture( Assets.Sprites.STRONG_WARLOCK );

		TextureFilm frames = new TextureFilm( texture, 12, 15 );

        int x = 21;

		idle = new Animation( 2, true );
		idle.frames( frames, x, x, x, x+1, x, x, x+1, x+1 );
		
		run = new Animation( 15, true );
		run.frames( frames, x, x+2, x+3, x+4 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, x, x+5, x+6 );

		die = new Animation( 10, false );
        die.frames( frames, x, x );
		
		play( idle );
	}

    @Override
    public void die() {
        super.die();

        particles = emitter();
        particles.pour( ShadowParticle.UP, 0.02f );
        particles.visible = visible;
        particles.on = true;
    }

    @Override
    public void kill() {
        super.kill();
        if (particles != null){
            particles.killAndErase();
        }
    }

    @Override
    public int blood() {
        return 0x88000000;
    }

    @Override
    public void onComplete(Animation anim) {
        if (anim == die && particles != null){
            particles.on = false;
        }
        super.onComplete(anim);
    }
}
