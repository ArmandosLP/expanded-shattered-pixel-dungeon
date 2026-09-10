package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public abstract class ShamanBlessing extends FlavourBuff {

    public static final float DURATION = 10f;

    {
        type = buffType.POSITIVE;

        announced = false;
        mnemonicExtended = false;
    }

    public static class Red extends ShamanBlessing {
        @Override
        public int icon() {
            return BuffIndicator.RED_SHAMAN_BLESSING;
        }
    }

    public static class Blue extends ShamanBlessing {
        @Override
        public int icon() {
            return BuffIndicator.BLUE_SHAMAN_BLESSING;
        }
    }

    public static class Purple extends ShamanBlessing {
        @Override
        public int icon() { return BuffIndicator.PURPLE_SHAMAN_BLESSING; }
    }

}

