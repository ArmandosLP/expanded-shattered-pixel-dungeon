package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Frenzy extends Buff{

    private int left = 0;
    private float dmgBoost = 0f;

    {
        type = buffType.POSITIVE;

        announced = true;
        mnemonicExtended = false;
    }

    public int proc( int damage ) {
        left--;
        if (left <= 0) detach();
        return (int)(damage * (dmgBoost + 1f));
    }

    public void set(int hits, float dmgBoost) {
        if (dmgBoost <= 0 || hits <= 0) {
            detach();
            return;
        }

        // Overwrite only if the dmgBoost is greater
        if (dmgBoost > this.dmgBoost) {
            left = hits;
            this.dmgBoost = dmgBoost;
            mnemonicExtended = false; // We consider it as a new effect so it can be buffd again
        }
    }

    public void mnemonicBoost( int value ) {
        left += value;
    }

    @Override
    public int icon() {
        return BuffIndicator.FRENZY;
    }

    @Override
    public String iconTextDisplay() {
        if (left > 1){
            return Integer.toString(left);
        }
        return "";
    }

    @Override
    public String desc() {
        return Messages.get(
                this,
                "desc",
                left,
                Messages.decimalFormat("#.##", 100 * dmgBoost)
        );
    }

    private static final String LEFT = "left";
    private static final String DAMAGE_BOOST = "damage_boost";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( LEFT, left );
        bundle.put( DAMAGE_BOOST, dmgBoost );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        left = bundle.getInt( LEFT );
        dmgBoost = bundle.getFloat( DAMAGE_BOOST );
    }

}