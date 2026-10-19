package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.DemonicFire;
import com.shatteredpixel.shatteredpixeldungeon.expanded.effects.particles.DemonicFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class DemonicFlame extends Blob {

    {
        actPriority = MOB_PRIO -1; // We want mobs and hero to be affected immediately after entering blob area.
    }

    @Override
    public void use( BlobEmitter emitter ) {
        super.use( emitter );
        emitter.pour( DemonicFlameParticle.FACTORY, 0.02f );
    }

    @Override
    protected void evolve() {
        volume -= 1;
        for (int i = area.left; i < area.right; i++){
            for (int j = area.top; j < area.bottom; j++){
                int cell = i + j*Dungeon.level.width();

                if (cur[cell] > 0){
                    Char ch = Char.findChar(cell);
                    if (ch != null){
                       burn(ch);
                    }
                }

                off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;

                off[cell] = Math.min(off[cell], 6); //caps at 6 turns, enough for eye death gaze to shoot again. Absolutely diabolical >:)
                volume += off[cell];
            }
        }
    }

    public static void burn( Char ch ) {
        if (!ch.isImmune(DemonicFlame.class)) {
            Buff.affect(ch, DemonicFire.class).ignite(ch);
        }
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}
