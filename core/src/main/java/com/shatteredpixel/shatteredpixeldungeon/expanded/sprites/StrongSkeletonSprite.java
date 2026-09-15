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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger.StrongSkeleton;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

public class StrongSkeletonSprite extends MobSprite {

	public StrongSkeletonSprite() {
		super();
		texture( Assets.Sprites.STRONG_SKELETON );
	}

    private void updateAnimations(){
        int c = 17*weapon.ordinal();

        TextureFilm frames = new TextureFilm( texture, 15, 15 );

        idle = new Animation( 12, true );
        idle.frames( frames, c, c, c, c, c, c, c, c, c, c, c, c, c, c+1, c+2, c+3 );

        run = new Animation( 15, true );
        run.frames( frames, c+4, c+5, c+6, c+7, c+8, c+9 );

        attack = new Animation( 15, false );
        attack.frames( frames, c+14, c+15, c+16 );

        die = new Animation( 12, false );
        die.frames( frames, c+10, c+11, c+12, c+13 );

        play( idle );
    }

    public StrongSkeleton.Weapon weapon = StrongSkeleton.Weapon.NONE;

    public void setWeapon(StrongSkeleton.Weapon weapon){
        this.weapon = weapon;
        updateAnimations();
    }

    public void weaponBreakEffect(){
        emitter().burst(EarthParticle.FACTORY, 3 );
        Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1,0.8f);
    }

    @Override
	public void die() {
		super.die();
		if (Dungeon.level.heroFOV[ch.pos]) {
			emitter().burst( Speck.factory( Speck.BONE ), 6 );
		}
	}

	@Override
	public int blood() {
		return 0xFFcccccc;
	}

}