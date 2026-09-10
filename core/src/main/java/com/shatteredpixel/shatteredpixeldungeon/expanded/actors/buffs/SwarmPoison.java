package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.awt.datatransfer.FlavorEvent;

public class SwarmPoison extends Buff implements Hero.Doom{

    private int left = 0;

    {
        type = buffType.NEGATIVE;
    }

    @Override
    public boolean act() {
        int stage = stage();

        // Starting from fever immune system stops working
        if (stage <= STAGE_2) { left -= 1; }
        else { left += 1; }

        if (left <= 0) {detach();}

        // Death sentence in stage 4, antidote obligatory
        if (stage == STAGE_4){
            target.damage(1 + ((left - DELIRIUM) / 100),this);
        }

        // STAGE_3 acts as a FlavourBuff, see this.speedFactor() and Char.spend()

        if (stage >= STAGE_2 && Random.Int(DELIRIUM - left) == 0){
            if (target instanceof Hero) {((Hero) target).interrupt();} // Interrupt the player to avoid unfair pit falling
            Buff.prolong(target, Vertigo.class, 1 + ((float) (left - IRRITATION) / 50));
        }

        // STAGE_1 dose nothing

        spend( TICK );
        return true;
    }

    // Cap to 20% so player dies while crawling
    public float speedFactor(){
        return Math.max(0.2f, 1f - (float)(left - FEVER) / 200);
    }

    public void extend(int amount) {
        left = Math.max(0,left + amount) ;
        if (left == 0) {detach();}
    }

    public static final int STAGE_1 = 1; // Itch
    public static final int STAGE_2 = 2; // Irritation
    public static final int STAGE_3 = 3; // Fever
    public static final int STAGE_4 = 4; // Delirium

    public static final int ITCH = 0;
    public static final int IRRITATION = 200;
    public static final int FEVER = 350;
    public static final int DELIRIUM = 500;

    public int stage() {
        if (left >= DELIRIUM){return STAGE_4;}
        if (left >= FEVER){return STAGE_3;}
        if (left >= IRRITATION){return STAGE_2;}
        return STAGE_1;
    }

    @Override
    public int icon() {
        switch (stage()){
            case STAGE_4: return BuffIndicator.SWARM_POISON_4;
            case STAGE_3: return BuffIndicator.SWARM_POISON_3;
            case STAGE_2: return BuffIndicator.SWARM_POISON_2;
            case STAGE_1: default: return BuffIndicator.SWARM_POISON_1;
        }
    }

    @Override
    public String name() {
        return Messages.get(this, "name_" + stage());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc_" + stage(), left);
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

    @Override
    public void onDeath() {
        Dungeon.fail(Swarm.class); // We assume swarm is the killer
        GLog.n( Messages.get(SwarmPoison.class, "ondeath") );
    }

}