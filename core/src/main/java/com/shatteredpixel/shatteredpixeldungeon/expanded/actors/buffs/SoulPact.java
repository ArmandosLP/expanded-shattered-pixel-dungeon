
package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import com.watabou.utils.Bundle;

public class SoulPact extends Buff implements Hero.Doom {
	
	private int level;

	private static final String LEVEL = "level";

	{
		type = buffType.NEUTRAL;
		announced = true;
		revivePersists = true;
	}

	public static final float DURATION	= 10f;
	
	@Override
	public int icon() {
		if (level <= 0){
			return BuffIndicator.NONE;
		}else{
			return BuffIndicator.BLOOD_PACT;
		}
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1, 0, 0);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	public void addLevel(int val){
		level += val;
	}

	public int value(){
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

    public String desc(){
        return Messages.get(this, "desc", value());
    }


	@Override
	public void onDeath() {
		// This is useless, the hero cant die from a soul pact, but a leave it just in case
		//Dungeon.fail( this );
		GLog.n( "How did you manage to die from the soul pact?, it should be impossible!" );
	}
	
}
