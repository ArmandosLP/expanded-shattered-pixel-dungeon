package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DM100Explosive extends Blob {



//    @Override
//    public Notes.Landmark landmark() {
//        return Notes.Landmark.ALCHEMY;
//    }


    @Override
    public boolean act() {
        zapAround(cur[0]);
        return super.act();
    }

    @Override
    protected void evolve() {
        int cell;
        for (int i=area.top-1; i <= area.bottom; i++) {
            for (int j = area.left-1; j <= area.right; j++) {
                cell = j + i* Dungeon.level.width();
                if (Dungeon.level.insideMap(cell)) {
                    off[cell] = cur[cell];

                    volume += off[cell];
                }
            }
        }
    }
//
//        int cell;
//        for (int i=area.top-1; i <= area.bottom; i++) {
//            for (int j = area.left-1; j <= area.right; j++) {
//                cell = j + i* Dungeon.level.width();
//                if (Dungeon.level.insideMap(cell)) {
//                    off[cell] = cur[cell];
//
//                    volume += off[cell];
//                }
//            }
//        }

/*
    @Override
    protected void evolve() {
        int cell;
        for (int i=area.top-1; i <= area.bottom; i++) {
            for (int j = area.left-1; j <= area.right; j++) {
                cell = j + i* Dungeon.level.width();
                if (Dungeon.level.insideMap(cell)) {
                    off[cell] = cur[cell];

                    volume += off[cell];
                }
            }
        }
    }
*/
    @Override
    public void use( BlobEmitter emitter ) {
        super.use( emitter );
        emitter.start( Speck.factory( Speck.BONE ), 0.33f, 0 );
    }

    public void zapAround(int pos) {

    }

//    public static void shockChar(Char ch){
//        if (ch != null){
//            ch.sprite.flash();
//            ch.damage(Random.NormalIntRange(3, 5), new Electricity());
//            if (!ch.isAlive()) {
//                // Dungeon.fail(attacker);
//                GLog.n(Messages.get(Electricity.class, "ondeath"));
//            }
//        }
//
//
//    }
}
