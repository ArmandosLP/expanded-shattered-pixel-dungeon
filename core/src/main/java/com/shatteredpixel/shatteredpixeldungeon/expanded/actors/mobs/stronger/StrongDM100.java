package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;

public class StrongDM100 extends DM100{

    {
        HP = HT = 1;
    }

//    @Override
//    public void die(Object cause) {
//        super.die(cause);
//
//        if (Dungeon.level.pit[ pos ]) return;
//
//        int ofs;
//        do { ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
//        } while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
//
//        DM100Explosive explosive = new DM100Explosive();
//        explosive.startFuse();
//        Dungeon.level.drop( explosive, pos + ofs).sprite.drop( pos );
//
////        Dungeon.level.drop( new ScrollOfRemoveCurse(), pos + ofs ).sprite.drop( pos );
//
////        .sprite.drop( pos )
////        GameScene.add(Blob.seed(pos, 1, DM100Explosive.class));
//    }



}



