package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Sticky extends Buff{

    {
        type = buffType.NEGATIVE;
    }

    private int lastTargetPos = -1;

    @Override
    public boolean act() {
        if (lastTargetPos == target.pos) {
            detach();
        }

        lastTargetPos = target.pos;

        spend( target.cooldown() );
        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.STICKY;
    }

    private static final String LAST_TARGET_POS = "last_target_pos";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LAST_TARGET_POS,lastTargetPos);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        lastTargetPos = bundle.getInt(LAST_TARGET_POS);
    }

}