package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.curse;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class WarlockCurse extends Buff{

    {
        type = buffType.NEGATIVE;

        announced = false;
        mnemonicExtended = true;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

    public static final ArrayList<Class<? extends WarlockCurse>> curses = new ArrayList<>();

    static {
        curses.add(CurseOfCondemnation.class);
        curses.add(CurseOfDeterioration.class);
    }

    public static Class<? extends WarlockCurse> random(Char target){

        ArrayList<Class<? extends WarlockCurse>> availableCourses = new ArrayList<>();

        for (Class<? extends WarlockCurse> curse : curses) {
            if (target.buff(curse) == null){ availableCourses.add(curse); }
        }

        if (!availableCourses.isEmpty()){
            Collections.shuffle(availableCourses);
            return Random.element(availableCourses);
        }

        return null;
    }


}
