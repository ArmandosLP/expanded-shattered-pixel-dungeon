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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger.StrongDM200;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.watabou.noosa.particles.Emitter;

public class StrongDM200Sprite extends DM200Sprite {


    private Emitter superchargeSparks;
    public void updateChargeState( boolean enraged ) {
        if (superchargeSparks != null) superchargeSparks.on = enraged;
    }

    @Override
    public void link(Char ch) {
        super.link(ch);

        superchargeSparks = emitter();
        superchargeSparks.autoKill = false;
        superchargeSparks.pour(SparkParticle.STATIC, 0.05f);
        superchargeSparks.on = false;

        if (ch instanceof StrongDM200 && ((StrongDM200) ch).isOvercharged()){
            updateChargeState(true);
        }
    }

    @Override
    public void update() {
        super.update();

        if (superchargeSparks != null){
            superchargeSparks.visible = visible;
        }
    }

    @Override
    public void die() {
        super.die();
        if (superchargeSparks != null){
            superchargeSparks.on = false;
        }
    }

    @Override
    public void kill() {
        super.kill();
        if (superchargeSparks != null){
            superchargeSparks.killAndErase();
        }
    }

}
