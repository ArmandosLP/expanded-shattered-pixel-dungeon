/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.expanded.actors.mobs.stronger;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfDread;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;

public class StrongDemonSpawner extends DemonSpawner {

    @Override
    public void damage(int dmg, Object src) {
        if (src instanceof Scroll || src instanceof Wand){
            triggerReflect(src);
        }
        super.damage(dmg, src);
    }

    public void triggerReflect(Object src){
        // We are add an actor to act immediately after the magic trigger to prevent the reflection effect from activating mid player action
        add(new MagicReflect(src, sprite.center(), Dungeon.hero.sprite.center()));
    }

    @Override
    public String description() {
        return super.description() + "\n\n" + Messages.get(this, "talent");
    }

    public static class MagicReflect extends Actor{
        {
            actPriority = VFX_PRIO;
        }

        private final Object src;
        private final PointF beamOrigin;
        private final PointF beamTarget;

        public MagicReflect(Object src, PointF beamOrigin, PointF beamTarget){
            this.beamOrigin = beamOrigin;
            this.beamTarget = beamTarget;
            this.src = src;
        }

        @Override
        protected boolean act() {
            ShatteredPixelDungeon.scene().add(new Beam.DeathRay(beamOrigin, beamTarget));
            Sample.INSTANCE.play( Assets.Sounds.RAY );

            if (!Dungeon.hero.isAlive()) {
                Actor.remove(this);
                return true;
            }

            if (src instanceof Item){
                GLog.w(Messages.get(StrongDemonSpawner.class, "reflect"), ((Item) src).name());
            }

            Sample.INSTANCE.play( Assets.Sounds.CURSED );
            CellEmitter.get(Dungeon.hero.pos).burst(ShadowParticle.CURSE, 6);

            if (src instanceof ScrollOfPsionicBlast){
                Dungeon.hero.damage(500, StrongDemonSpawner.class);
            }else if (src instanceof ScrollOfRetribution){
                Dungeon.hero.damage(50, StrongDemonSpawner.class);
            } else if (src instanceof ScrollOfTerror) {
                Buff.affect(Dungeon.hero,PlayerTerror.class).set(PlayerTerror.DURATION);
            } else if (src instanceof ScrollOfDread) {
                Buff.affect(Dungeon.hero,PlayerDread.class).set(PlayerDread.DURATION);
            } else if (src instanceof ScrollOfLullaby) {
                Buff.affect(Dungeon.hero,EternalDrowsy.class,EternalDrowsy.DURATION);
            }

            Actor.remove( this );
            return true;
        }

    }

    public static class PlayerTerror extends Buff{
        {
            announced = true;
            type = buffType.NEGATIVE;
        }

        public static final int DURATION = 100;

        private int left = 0;

        private static final String LEFT = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getInt(LEFT);
        }

        @Override
        public String name() {
            return Messages.get(Terror.class,"name");
        }

        @Override
        public String desc() {
            return Messages.get(this,"desc", left);
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString((int)left);
        }

        @Override
        public int icon() {
            return BuffIndicator.TERROR;
        }

        public void set(int left){
            this.left = left;
        }

        @Override
        public boolean act() {
            if (target == null || !(target instanceof Hero) || !target.isAlive()) {
                detach();
                return true;
            }

            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                // We only mob.beckon if mob is not hunting and hero is not visible to avoid alert spam
                if (mob.alignment == Alignment.ENEMY && !mob.isCharmedBy(Dungeon.hero) && mob.state != mob.HUNTING && !mob.fieldOfView[Dungeon.hero.pos]){
                    mob.beckon(Dungeon.hero.pos);
                    mob.sprite.hideAlert();
                }

            }

            left -= 1;
            if (left <= 0){
                detach();
            }

            spend(TICK);
            return true;
        }
    }

    public static class PlayerDread extends PlayerTerror{
        {
            announced = true;
            type = buffType.NEUTRAL; // Neutral so it cannot be cleansed
        }

        public static final int DURATION = 1000;

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1, 0, 0);
        }

        @Override
        public String name() {
            return Messages.get(Dread.class,"name");
        }
    }

    public static class EternalDrowsy extends FlavourBuff{
        {
            announced = true;
            type = buffType.NEUTRAL;
        }

        public static final float DURATION = 5f;

        @Override
        public String name() {
            return Messages.get(Drowsy.class, "name");
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1, 0, 0);
        }

        @Override
        public int icon() {
            return BuffIndicator.DROWSY;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

        public boolean attachTo(Char target) {
            if (!target.isImmune(Sleep.class) && super.attachTo(target)) {
                Drowsy drowsy = target.buff(Drowsy.class);
                if (drowsy != null) { drowsy.detach(); }
                return true;
            }
            return false;
        }

        @Override
        public boolean act(){
            Buff cleanse = target.buff(PotionOfCleansing.Cleanse.class);
            if (cleanse != null) cleanse.detach(); // No mercy
            Buff.affect(target, EternalSleep.class).set(EternalSleep.DURATION);
            return super.act();
        }
    }

    public static class EternalSleep extends Buff{
        {
            announced = false;
            type = buffType.NEGATIVE;
        }
        public static final int STEP = 1;

        public static final int DURATION = 2500;
        private int left;

        public void set(int duration){
            left = duration;
        }

        @Override
        public boolean act() {
            // Let the player sleep to death muahahaha!
            Dungeon.hero.resting = true;
            left -= STEP;
            spend( STEP );
            if (left <= 0) detach();
            return true;
        }

        @Override
        public boolean attachTo(Char target) {
            if (target instanceof Hero && super.attachTo(target)) {
                target.paralysed += 1;
                return true;
            }
            return false;
        }

        @Override
        public void detach() {
            if (target.paralysed >= 0){
                target.paralysed --;
            }
            Dungeon.hero.resting = false;
            super.detach();
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }

        @Override
        public int icon() {
            return BuffIndicator.MAGIC_SLEEP;
        }

        @Override
        public void fx(boolean on) {
            if (!on && (target.paralysed <= 1) ) {
                //in case the character has visual paralysis from another source
                target.sprite.remove(CharSprite.State.PARALYSED);
            }
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1, 0, 0);
        }

        private static final String LEFT = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getInt(LEFT);
        }
    }
}
