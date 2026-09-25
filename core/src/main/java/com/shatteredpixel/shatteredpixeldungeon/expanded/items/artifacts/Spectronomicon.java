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

package com.shatteredpixel.shatteredpixeldungeon.expanded.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.expanded.sprites.WeakWraithSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Spectronomicon extends Artifact {

	{
        image = ItemSpriteSheet.SPECTERNOMICON;

		levelCap = 10;

        exp = 0;
        charge = (level()/2)+2;
        partialCharge = 0;
        chargeCap = (int)(level()/2.5f)+2;

		defaultAction = AC_SUMMON;
	}

    @Override
    public String name() {
        return super.name();
    }

    private boolean partialLevel = false;
    public static final int MULTI_SUMMON_CHARGES = 3;
    public static final int SUMMON_CHARGES = 1;
    public static final float READ_TICK = 1f;
    public static final float ABSORB_TICK = 2f;
	public static final String AC_SUMMON = "SUMMON";
	public static final String AC_CONSUME = "CONSUME";

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && charge >= SUMMON_CHARGES && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_SUMMON);
        }
        if (isEquipped( hero ) && level() < levelCap && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_CONSUME);
        }
        return actions;
    }

    @Override
    public void resetForTrinity(int visibleLevel) {
        super.resetForTrinity(visibleLevel);
        charge = (visibleLevel / 2);
    }

    public static void summon(int cell){
        WeakWraith.spawn(cell, Dungeon.hero);
    }

    public static void multiSummon(int cell){
        for (int c : PathFinder.NEIGHBOURS4) {
            if ((Dungeon.level.openSpace[cell + c] || Dungeon.level.passable[cell + c]) && Actor.findChar(cell + c) == null) {
                summon(cell + c);
            }
        }
    }

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals( AC_SUMMON )) {

			if (hero.buff( Blindness.class ) != null) GLog.w( Messages.get(this, "blinded") );
			else if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge < SUMMON_CHARGES)         GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                          GLog.i( Messages.get(this, "cursed") );
			else {
                doReadEffect();
			}

		}
        else if (action.equals( AC_CONSUME )) {
			GameScene.selectItem(itemSelector);
		}
	}

    public void doReadEffect(){
        GameScene.selectCell(summonTargeter);
    }

    public Item upgrade(boolean partial) {
        if (partial){
            if (partialLevel) upgrade();
            partialLevel = !partialLevel;
            return this;
        }
        return upgrade();
    }

    @Override
    public Item upgrade() {
        chargeCap = (int)((level() + 1)/2.5f)+2;
        return super.upgrade();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null){
            partialCharge += 0.1f*amount;
            while (partialCharge >= 1){
                partialCharge--;
                charge++;
            }
            if (charge >= chargeCap){
                partialCharge = 0;
            }
            updateQuickslot();
        }
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new bookRecharge();
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped( Dungeon.hero )){
            if (cursed)
                desc += "\n\n" + Messages.get(this, "desc_cursed");
            else if (level() < levelCap) {
                desc += "\n\n" + Messages.get(this, "upgrade_desc");
            }
        }

        return desc;
    }

    private static final String PARTIAL_LEVEL = "partial_Level";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PARTIAL_LEVEL, partialLevel);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        partialLevel = bundle.getBoolean(PARTIAL_LEVEL);
    }

    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(Spectronomicon.class, "prompt_consume");
		}

		@Override
		public boolean itemSelectable(Item item) {
            return item instanceof Weapon || item instanceof Armor;
		}

		@Override
		public void onSelect(Item item) {
            if (item == null){ return; }

            if (!item.cursedKnown){
                GLog.i( Messages.get(Spectronomicon.class, "consume_cursed_unknown") );
                return;
            }

            if (item.isEquipped(Dungeon.hero)){
                GLog.i( Messages.get(Spectronomicon.class, "consume_equipped") );
                return;
            }

            int level = level();

            if (item instanceof Weapon){

                Weapon it = (Weapon) item;
                if (it.enchantment != null){
                    if (it.enchantment.curse()) {
                        upgrade(true);
                    }else{
                        upgrade();
                    }
                    it.enchant(null);
                }else{
                    GLog.i( Messages.get(Spectronomicon.class, "consume_no_magic") );
                    return;
                }

            } else if (item instanceof Armor) {

                Armor it = (Armor) item;
                if (it.glyph != null){
                    if (it.glyph.curse()) {
                        upgrade(true);
                    }else{
                        upgrade();
                    }
                    it.inscribe(null);
                }else{
                    GLog.i( Messages.get(Spectronomicon.class, "consume_no_magic") );
                    return;
                }
            }

            Hero hero = Dungeon.hero;
            hero.sprite.operate( hero.pos );
            hero.busy();
            hero.spend( ABSORB_TICK );
            Sample.INSTANCE.play(Assets.Sounds.DRINK, 1.5f, Random.Float(1.5f,1.8f));
            Sample.INSTANCE.play(Assets.Sounds.DRINK, 1.5f, Random.Float(1.5f,1.8f));
            hero.sprite.emitter().burst( ShadowParticle.CURSE, 6 );

            Catalog.countUse(Spectronomicon.class);

            if (level != level()){
                GLog.i( Messages.get(Spectronomicon.class, "upgrade") );
            }else{
                GLog.i( Messages.get(Spectronomicon.class, "upgrade_partial") );
            }
		}
	};

    public class bookRecharge extends ArtifactBuff{
        @Override
        public boolean act() {
            if (charge < chargeCap
                    && !cursed
                    && target.buff(MagicImmune.class) == null
                    && Regeneration.regenOn()) {
                //120 turns to charge at full, 80 turns to charge at 0/8
                float chargeGain = 1 / (120f - (chargeCap - charge)*5f);
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);
                partialCharge += chargeGain;

                while (partialCharge >= 1) {
                    partialCharge --;
                    charge ++;

                    if (charge == chargeCap){
                        partialCharge = 0;
                    }
                }
            }

            updateQuickslot();
            spend( TICK );
            return true;
        }
    }

    public final CellSelector.Listener summonTargeter = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;

            if (!Dungeon.level.openSpace[cell] && !Dungeon.level.passable[cell]){
                GLog.i( Messages.get(Spectronomicon.class, "no_space") );
                return;
            }

            if (!Dungeon.hero.fieldOfView[cell]){
                GLog.i( Messages.get(Spectronomicon.class, "out_of_fov") );
                return;
            }

            PathFinder.buildDistanceMap(cell, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
            if (!(Dungeon.level instanceof MiningLevel) && PathFinder.distance[curUser.pos] == Integer.MAX_VALUE){
                GLog.i( Messages.get(Spectronomicon.class, "no_space") );
                return;
            }

            Char ch = Actor.findChar(cell);

            if (ch != null){
                // Check alignment to avoid mimics
                if (ch.alignment != Char.Alignment.ENEMY){
                    GLog.i( Messages.get(Spectronomicon.class, "no_space") );
                    return;
                }

                if (charge < MULTI_SUMMON_CHARGES){
                    GLog.i(Messages.get(Spectronomicon.class, "no_charge_multi_summon"));
                    return;
                }

                charge -= MULTI_SUMMON_CHARGES;
                multiSummon(cell);
            }else{
                charge -= SUMMON_CHARGES;
                summon(cell);
            }

            Sample.INSTANCE.play(Assets.Sounds.READ);
            curUser.spend( READ_TICK );
            curUser.busy();
            ((HeroSprite)curUser.sprite).read();

            Catalog.countUse(Spectronomicon.class);
            Invisibility.dispel(Dungeon.hero);
            Talent.onArtifactUsed(Dungeon.hero);
            updateQuickslot();
        }

        @Override
        public String prompt() {
            return Messages.get(Spectronomicon.class, "prompt_summon");
        }
    };

    public static class WeakWraith extends Wraith{
        {
            spriteClass = WeakWraithSprite.class;
            alignment = Alignment.ALLY;
            defenseSkill = 0;
        }

        private static final float SPAWN_DELAY	= 2f;
        protected int masterID;
        protected int timeToLive;

        public static void spawn(int cell, Char master){
            spawn(cell,master,3);
        }

        public static void spawn(int cell, Char master, int timeToLive){
            WeakWraith ww = new WeakWraith();;
            ww.masterID = master.id();

            ww.pos = cell;
            ww.timeToLive = timeToLive;
            GameScene.add( ww, SPAWN_DELAY );
            Dungeon.level.occupyCell(ww);

            ww.fieldOfView = new boolean[Dungeon.level.length()];
            Dungeon.level.updateFieldOfView( ww, ww.fieldOfView );

            int distToMob = 999;
            Mob mobToAgro = null;

            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (ww.fieldOfView[mob.pos] &&
                        mob != master &&
                        mob.alignment == Alignment.ENEMY &&
                        !mob.isCharmedBy(master) &&
                        !(ww.mobTaget(mob) instanceof WeakWraith))
                {
                    // Save the closest mob, if both mobs are equally close, choose one randomly
                    int dist = ww.distance(mob);
                    if (dist < distToMob){
                        mobToAgro = mob;
                        distToMob = dist;
                    } else if (dist == distToMob && Random.Int(2) == 0) {
                        mobToAgro = mob;
                    }
                }
            }

            if (mobToAgro != null){
                ww.agroMob(mobToAgro);
            }

            ww.sprite.place(cell);
            ww.state = ww.HUNTING;

            if (Dungeon.hero.fieldOfView != null && Dungeon.hero.fieldOfView[cell]) {
                ww.sprite.alpha(0);
                ww.sprite.parent.add(new AlphaTweener(ww.sprite, 1, 0.5f));
                ww.sprite.emitter().burst(ShadowParticle.CURSE, 5);
            }

        }

        protected void agroMob(Mob mob){
            mob.clearEnemy();
            mob.beckon(pos);
            mob.aggro(this);
        }

        @Override
        public void die(Object cause) {
            super.die(cause);
            sprite.emitter().burst(ShadowParticle.UP, 5);
        }

        private Char mobTaget(Mob mob){
            if (mob.isTargeting(null)){ return null; }

            for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (mob.isTargeting(m)){
                    return m;
                }
            }

            return null;
        }

        @Override
        protected boolean act() {
            if (Dungeon.level.heroFOV[pos]){
                Bestiary.setSeen(getClass());
            }

            Char master = (Char) findById(masterID);

            if (enemy != null && enemy instanceof Mob){
                Char enemyTarget = mobTaget((Mob) enemy);
                if (enemyTarget != null && !(enemyTarget instanceof WeakWraith)){
                    agroMob((Mob)enemy);;
                }
            }

            if ((master == null || !master.isAlive()) || timeToLive <= 0){
                die(null);
                return true;
            }
            if (enemy == null){ timeToLive -= 1; }
            return super.act();
        }

        // Extremely weak, not intended for combat
        @Override
        protected boolean canAttack(Char enemy) {
            return false;
        }

        @Override
        public int attackSkill( Char target ) {
            return 0;
        }

        private static final String TIME_TO_LIVE = "time_to_live";
        private static final String MASTER_ID = "master_id";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(MASTER_ID, masterID);
            bundle.put(TIME_TO_LIVE,timeToLive);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            masterID = bundle.getInt(MASTER_ID);
            timeToLive = bundle.getInt(TIME_TO_LIVE);
        }
    }

}
