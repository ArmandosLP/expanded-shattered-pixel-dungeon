package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.curse;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CurseOfDeterioration extends WarlockCurse{

    private int level = 0;

    public void proc( float damage ) {
        level += (int) Math.floor(damage * 0.8);
        if (target instanceof Hero){
            ((Hero) target).updateHT(false);
        }
    }

    public int boost(){
        return level * -1;
    }

    @Override
    public int icon() {
        return BuffIndicator.FRENZY;
    }

    @Override
    public String desc() {
        return super.desc() + Messages.get(this, "desc");
    }

    private static final String LEVEL = "level";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( LEVEL, level );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        level = bundle.getInt( LEVEL );
    }

}