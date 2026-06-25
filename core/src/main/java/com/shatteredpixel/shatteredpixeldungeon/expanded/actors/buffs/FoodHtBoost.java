package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Bundle;

public class FoodHtBoost extends Buff {
    private int level;

    private static final String LEVEL = "level";

    {
        type = buffType.NEUTRAL;
        revivePersists = true;
    }

    public void addLevel(int value){
        level += value;
    }

    public int boost(){
        return level;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( LEVEL, level );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        level = bundle.getInt(LEVEL);
    }
}
