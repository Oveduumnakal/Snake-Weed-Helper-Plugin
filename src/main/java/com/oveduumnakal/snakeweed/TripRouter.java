/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oveduumnakal.snakeweed;

import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;

/**
 * Decides, each tick, what route to show: the next vine while harvesting, the nearest bank when the backpack is
 * full of snake weed, nothing while the bank is open, and the nearest vine on the way back. It feeds
 * {@link TripTracker} what the player is doing and hands the matching targets to {@link ShortestPathBridge}.
 */
@Singleton
class TripRouter
{
	/** Tiles from a known vine within which the player counts as being at the vines. */
	static final int NEAR_VINES = 12;

	private static final int BACKPACK_SLOTS = 28;

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final RouteService route;
	private final ShortestPathBridge shortestPath;
	private final KnownVines knownVines;
	private final ClientRequirements requirements;
	private final BankTable banks = BankTable.load();
	private final TripTracker trip = new TripTracker();

	private Set<WorldPoint> bankTiles;

	@Inject
	TripRouter(Client client, SnakeWeedHelperConfig config, RouteService route, ShortestPathBridge shortestPath,
		KnownVines knownVines, ClientRequirements requirements)
	{
		this.client = client;
		this.config = config;
		this.route = route;
		this.shortestPath = shortestPath;
		this.knownVines = knownVines;
		this.requirements = requirements;
	}

	/** Goes back to harvesting and forgets the bank list, for logout or when trips are switched off. */
	void reset()
	{
		trip.reset();
		bankTiles = null;
	}

	/**
	 * Updates the trip from the player's backpack and bank, and shows the route for the new state.
	 *
	 * @param tick the current game tick
	 */
	void update(int tick)
	{
		Player me = client.getLocalPlayer();
		if (me == null)
			return;

		TripState state = TripState.HARVESTING;
		if (!config.bankTrips())
			reset();
		else
			state = trip.update(freeSlots(), carriesWeed(), bankOpen(), nearVines(me), config.bankWhenFreeSlots());

		if (state != TripState.TO_BANK)
			bankTiles = null;

		if (state != TripState.HARVESTING)
			route.reset();

		switch (state)
		{
			case TO_BANK:
				showBanks(me);
				break;
			case TO_VINES:
				shortestPath.show(me.getWorldLocation(), knownVines.tiles());
				break;
			case BANKING:
				shortestPath.clear();
				break;
			default:
				harvest(me, tick);
				break;
		}
	}

	/** Shows the next vine: rings it, and routes to a tile beside it unless already there. */
	private void harvest(Player me, int tick)
	{
		route.update(tick);
		WorldPoint approach = route.getApproach();
		if (approach != null)
			shortestPath.show(me.getWorldLocation(), Collections.singleton(approach));
		else
			shortestPath.clear();
	}

	/** Shows a route to the nearest bank the player can use. */
	private void showBanks(Player me)
	{
		if (bankTiles == null)
			bankTiles = banks.available(requirements);

		shortestPath.show(me.getWorldLocation(), bankTiles);
	}

	/** Empty backpack slots. */
	private int freeSlots()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return BACKPACK_SLOTS;

		int used = 0;
		for (Item item : inventory.getItems())
		{
			if (item.getId() != -1)
				used++;
		}

		return BACKPACK_SLOTS - used;
	}

	/** Whether any snake weed, grimy or clean, is in the backpack. */
	private boolean carriesWeed()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		return inventory != null && (inventory.contains(ItemID.UNIDENTIFIED_SNAKE_WEED)
			|| inventory.contains(ItemID.SNAKE_WEED));
	}

	/** Whether the bank interface is showing. */
	private boolean bankOpen()
	{
		Widget items = client.getWidget(InterfaceID.Bankmain.ITEMS);
		return items != null && !items.isHidden();
	}

	/** Whether the player is at the vines, which is also taken as true before any vine has been seen. */
	private boolean nearVines(Player me)
	{
		return knownVines.tiles().isEmpty() || knownVines.isNear(me.getWorldLocation(), NEAR_VINES);
	}
}
