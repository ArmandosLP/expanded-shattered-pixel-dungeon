package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class DemonicFire extends Buff implements Hero.Doom, Buff.DOTbuff {

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public static final float DURATION = 2;
    public static final float DURATION_FOR_DEMONIC = 4;
    private float left;
    private boolean justAttached = true;

    @Override
    public boolean act() {
        if (target.isAlive() && !target.isImmune(getClass())){
            if (!target.properties().contains(Char.Property.DEMONIC)){
                target.damage((int)left, this);
                target.needsIncomingDOTUpdate = true;
            }else{
                int heal = Random.IntRange(3,5);
                target.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
                target.HP = Math.min(target.HP + heal, target.HT);
            }
        }else{
            detach();
        }

        spend(TICK);
        left -= TICK;

        if (left <= 0) detach();

        target.needsIncomingDOTUpdate = true;
        return true;
    }

    public void ignite(Char ch) {
        if (ch.properties().contains(Char.Property.DEMONIC)){
            // Give extra buff to demonic enemies but prevents it from stacking
            ignite(ch, left <= 2 ? DURATION_FOR_DEMONIC : DURATION);
        }else{
            ignite(ch, DURATION);
        }
    }

    public void ignite(Char ch, float duration) {
        left += duration;
        ch.needsIncomingDOTUpdate = true;
    }



    @Override
    public boolean attachTo(Char target) {
        boolean result = super.attachTo(target);
        if (
                result &&
                target.sprite != null &&
                target.sprite.visible
        ){
            Sample.INSTANCE.play(Assets.Sounds.BURNING);
        }
        if (justAttached) spend(TICK); // Avoid damaging/healing just after death gaze
        justAttached = false;
        return result;
    }

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.add(CharSprite.State.DEMONIC_FLAME);
        else target.sprite.remove(CharSprite.State.DEMONIC_FLAME);
    }

    @Override
    public int icon() {
        return BuffIndicator.DEMONIC_FIRE;
    }

    @Override
    public void onDeath() {
        Dungeon.fail( this );
        GLog.n( Messages.get(this, "ondeath") );
    }

    @Override
    public int totalIncomingDMG() {
        if (target.properties().contains(Char.Property.DEMONIC)) return 0;
        return (int)((left+1f) * (left/2));
    }

    @Override
    public void detach() {
        target.needsIncomingDOTUpdate = true;
        super.detach();
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(left));
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int)left);
    }

    private static final String LEFT = "left";
    private static final String JUST_ATTACHED = "just_attached";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LEFT, left);
        bundle.put(JUST_ATTACHED, justAttached);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        left = bundle.getFloat(LEFT);
        justAttached = bundle.getBoolean(JUST_ATTACHED);
    }
}
