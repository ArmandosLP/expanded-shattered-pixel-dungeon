package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public abstract class ExpandedWeaponAbilities extends MeleeWeapon {

    /*
    * This class is meant to save all the weapon abilities for expanded mod weapons.
    * Expanded content is saved "shatteredpixeldungeon/expanded" and weapon abilities require
    * the usage of protected data inside "shatteredpixeldungeon/items/weapon/melee".
    * Not a great solution but it is organized and functional.
    */

    public static void headBlowAbility(Hero hero, Integer target, float dmgMulti, int dmgBoost, MeleeWeapon wep){
        if (target == null) {
            return;
        }

        Char enemy = Actor.findChar(target);
        if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
            GLog.w(Messages.get(wep, "ability_no_target"));
            return;
        }

        hero.belongings.abilityWeapon = wep;
        if (!hero.canAttack(enemy)){
            GLog.w(Messages.get(wep, "ability_target_range"));
            hero.belongings.abilityWeapon = null;
            return;
        }
        hero.belongings.abilityWeapon = null;

        hero.sprite.attack(enemy.pos, new Callback() {
            @Override
            public void call() {
                wep.beforeAbilityUsed(hero, enemy);

                AttackIndicator.target(enemy);
                if (hero.attack(enemy, dmgMulti, dmgBoost, Char.INFINITE_ACCURACY)) {
                    Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                    if (enemy.isAlive()){
                        if (enemy instanceof Mob && enemy.buff(StunDilay.class) == null && ((Mob) enemy).surprisedBy(hero)){
                            Buff.affect(enemy, Paralysis.class, 1);
                            Buff.affect(enemy, StunDilay.class, StunDilay.DURATION);
                        }
                    } else {
                        onAbilityKill(hero, enemy);
                    }
                }
                Invisibility.dispel();
                hero.spendAndNext(hero.attackDelay());
                wep.afterAbilityUsed(hero);
            }
        });

    }

    public static class StunDilay extends FlavourBuff {
        public static final float DURATION	= 10f;
    }

    public static void pierceAbility(Hero hero, Integer target, float dmgMulti, int dmgBoost, MeleeWeapon wep){
        if (target == null) {
            return;
        }

        Char enemy = Actor.findChar(target);
        if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
            GLog.w(Messages.get(wep, "ability_no_target"));
            return;
        }

        hero.belongings.abilityWeapon = wep;
        if (!hero.canAttack(enemy)){
            GLog.w(Messages.get(wep, "ability_target_range"));
            hero.belongings.abilityWeapon = null;
            return;
        }
        hero.belongings.abilityWeapon = null;

        hero.sprite.attack(enemy.pos, new Callback() {
            @Override
            public void call() {
                wep.beforeAbilityUsed(hero, enemy);
                AttackIndicator.target(enemy);
                if (hero.attack(enemy, dmgMulti, dmgBoost, Char.INFINITE_ACCURACY)) {
                    if (enemy.isAlive()) {
                        Buff.affect(enemy, Vulnerable.class, Vulnerable.DURATION / 2);
                    } else {
                        onAbilityKill(hero, enemy);
                    }
                    Buff.affect(hero, Bleeding.class).extend( wep.buffedLvl() + 1 );
                    Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                    Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1, 0.6f);
                }
                Invisibility.dispel();
                hero.spendAndNext(hero.attackDelay());
                wep.afterAbilityUsed(hero);
            }
        });
    }


}
