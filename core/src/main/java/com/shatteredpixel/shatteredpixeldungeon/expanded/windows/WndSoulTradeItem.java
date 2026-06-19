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

package com.shatteredpixel.shatteredpixeldungeon.expanded.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.expanded.actors.buffs.SoulPact;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;


import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;

public class WndSoulTradeItem extends WndInfoItem {

	private static final float GAP		= 2;
	private static final int BTN_HEIGHT	= 18;

	private WndBag owner;

	private boolean selling = false;

	//buying
	public WndSoulTradeItem(final Heap heap ) {

		super(heap);

		selling = false;

		Item item = heap.peek();

		float pos = height;

		// Armandos FIXME Only in English for now

        int price = item.soulValue();
		RedButton btnBuy = new RedButton(Messages.get(this, "pact", price)){
			@Override
			protected void onClick() {
				hide();
				buy( heap );
			}
		};

		btnBuy.setRect( 0, pos + GAP, width, BTN_HEIGHT );
		btnBuy.icon(new ItemSprite(ItemSpriteSheet.PACT_ICON));
		btnBuy.enable( true );
		add( btnBuy );

		pos = btnBuy.bottom();

		resize(width, (int) pos);
	}
	
	@Override
	public void hide() {
		
		super.hide();
		CurrencyIndicator.showGold = false;
		
		if (owner != null) {
			owner.hide();
		}
		if (selling) Shopkeeper.sell();
	}

	private void buy( Heap heap ) {
		
		Item item = heap.pickUp();
		if (item == null) return;
		
		int price = item.soulValue();

		SoulPact buff = Buff.affect(Dungeon.hero, SoulPact.class);
		buff.addLevel(price);
		Dungeon.hero.updateHT( false );

		if (!item.doPickUp( Dungeon.hero )) {
			Dungeon.level.drop( item, heap.pos ).sprite.drop();
		}
	}

}
