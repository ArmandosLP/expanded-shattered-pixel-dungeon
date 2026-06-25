package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Ferocity extends Buff{

    private int left = 0;

    {
        type = buffType.POSITIVE;

        announced = true;
        mnemonicExtended = false;
    }

    public int proc( int damage ) {
        left--;
        if (left <= 0) detach();
        return damage + left + 1;
    }

    public void set(int duration) {
        if (duration <= 0) {
            detach();
            return;
        }

        if (duration > left){
            left = duration;
            mnemonicExtended = false; // We consider it as a new effect so it can be buffd again
        }
    }

    public void mnemonicBoost( int value ) {
        left += value;
    }

    @Override
    public int icon() {
        return BuffIndicator.FEROCITY;
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(left);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", left);
    }

    private static final String LEFT = "left";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( LEFT, left );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        left = bundle.getInt( LEFT );
    }

}