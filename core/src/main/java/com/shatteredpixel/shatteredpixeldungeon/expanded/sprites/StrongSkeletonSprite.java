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
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class StrongSkeletonSprite extends MobSprite {

    public boolean usingWeapon = true;
    protected Animation weaponIdle;
    protected Animation weaponRun;
    protected Animation weaponAttack;
    protected static final int SPEAR = 34;
    protected static final int SWORD     = 51;
    protected static final int SHIELD    = 17;

	public StrongSkeletonSprite() {
		super();

		texture( Assets.Sprites.STRONG_SKELETON );

		TextureFilm frames = new TextureFilm( texture, 15, 15 );

        idle = new Animation( 12, true );
        idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3 );

        run = new Animation( 15, true );
        run.frames( frames, 4, 5, 6, 7, 8, 9 );

        attack = new Animation( 15, false );
        attack.frames( frames, 14, 15, 16 );

        die = new Animation( 12, false );
        die.frames( frames, 10, 11, 12, 13 );

	}

    public void weaponBreak() {
        usingWeapon = false;
        idle();
        emitter().burst(EarthParticle.FACTORY, 3 );
        Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1,0.8f);
    }

    @Override
    public void play(Animation anim) {
        super.play(anim);
    }

    @Override
    public void idle() {
        if (usingWeapon) {
            play( weaponIdle );
        } else {
            play( idle );
        }

    }

    @Override
    public void move( int from, int to ) {
        super.move(from,to);
        if (usingWeapon) { play( weaponRun ); }
        else { play( run ); }
    }

    @Override
    public synchronized void attack( int cell, Callback callback ) {
        super.attack(cell, callback);
        if (usingWeapon) { play(weaponAttack); }
        else { play( attack ); }
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

    @Override
    public void onComplete(Animation anim) {
        if (anim == weaponAttack) super.onComplete(attack);
        if (anim == weaponRun) super.onComplete(run);
        if (anim == weaponIdle) super.onComplete(idle);
        super.onComplete(anim);
    }

    public static class Spear extends StrongSkeletonSprite {
        {
            int weapon = SPEAR;

            TextureFilm frames = new TextureFilm( texture, 15, 15 );

            weaponIdle = new Animation( 12, true );
            weaponIdle.frames( frames, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon+1, weapon+2, weapon+3 );

            weaponRun = new Animation( 15, true );
            weaponRun.frames( frames, weapon+4, weapon+5, weapon+6, weapon+7, weapon+8, weapon+9 );

            weaponAttack = new Animation( 15, false );
            weaponAttack.frames( frames, weapon+14, weapon+15, weapon+16 );

            idle();
        }
    }


    public static class Sword extends StrongSkeletonSprite {
        {
            int weapon = SWORD;

            TextureFilm frames = new TextureFilm( texture, 15, 15 );

            weaponIdle = new Animation( 12, true );
            weaponIdle.frames( frames, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon+1, weapon+2, weapon+3 );

            weaponRun = new Animation( 15, true );
            weaponRun.frames( frames, weapon+4, weapon+5, weapon+6, weapon+7, weapon+8, weapon+9 );

            weaponAttack = new Animation( 15, false );
            weaponAttack.frames( frames, weapon+14, weapon+15, weapon+16 );

            idle();
        }
    }

    public static class Shield extends StrongSkeletonSprite {
        {
            int weapon = SHIELD;

            TextureFilm frames = new TextureFilm( texture, 15, 15 );

            weaponIdle = new Animation( 12, true );
            weaponIdle.frames( frames, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon, weapon+1, weapon+2, weapon+3 );

            weaponRun = new Animation( 15, true );
            weaponRun.frames( frames, weapon+4, weapon+5, weapon+6, weapon+7, weapon+8, weapon+9 );

            weaponAttack = new Animation( 15, false );
            weaponAttack.frames( frames, weapon+14, weapon+15, weapon+16 );

            idle();
        }
    }

}
