package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.curse;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CurseOfCondemnation extends WarlockCurse{

    private float level = 0.0f;

    public float proc( float damage ) {
        float finalDamage;

        if ( level > Random.Float() ){
            finalDamage = damage * 100;
            CellEmitter.get(target.pos).burst(ShadowParticle.CURSE, 6);
            Sample.INSTANCE.play(Assets.Sounds.CURSED);
            detach();
        }else{
            finalDamage = damage;
            level += 0.01f;
        }

        return finalDamage;
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
        level = bundle.getFloat( LEVEL );
    }

}