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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InventoryID;

/**
 * Works out, each tick, the short warnings to show: missing supplies and a world that is too crowded. Both only
 * show while the player is at the vines, so they do not nag elsewhere.
 */
@Singleton
class HintService
{
	/** Tiles around the player within which another player counts as nearby. */
	static final int NEARBY_RADIUS = 15;

	/** Tiles from a seen vine within which the player counts as being at the vines. */
	static final int NEAR_VINES = 12;

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;

	private List<String> lines = Collections.emptyList();

	@Inject
	HintService(Client client, SnakeWeedHelperConfig config, VineTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.tracker = tracker;
	}

	/** The warnings to show now, empty when there are none. */
	List<String> getLines()
	{
		return lines;
	}

	/** Drops the warnings, for logout or when the plugin stops. */
	void reset()
	{
		lines = Collections.emptyList();
	}

	/**
	 * Recomputes the warnings.
	 *
	 * @param tick the current game tick
	 */
	void update(int tick)
	{
		Player me = client.getLocalPlayer();
		if (me == null || !nearVines(me.getWorldLocation()))
		{
			reset();
			return;
		}

		List<String> next = new ArrayList<>();
		if (config.warnMissingSupplies())
		{
			List<String> missing = InventoryCheck.missing(inventoryIds());
			if (!missing.isEmpty())
				next.add("Missing: " + String.join(", ", missing));
		}

		if (config.gloryHint() && GloryHint.shouldPrompt(freeSlots(), wornAmulet()))
			next.add(GloryHint.TEXT);

		if (config.hopHint() && WorldHopAdvisor.shouldHop(nearbyPlayers(me), ripeSoon(tick),
			config.hopMinPlayers(), config.hopMinRipeVines()))
			next.add("Crowded: consider hopping worlds");

		lines = next;
	}

	/** Whether a vine has been seen within {@link #NEAR_VINES} tiles, so the hints stay quiet away from the vines. */
	private boolean nearVines(WorldPoint here)
	{
		for (Vine vine : tracker.all())
		{
			if (vine.getTile().distanceTo(here) <= NEAR_VINES)
				return true;
		}

		return false;
	}

	/** Empty backpack slots, or {@code -1} when the backpack has not loaded, so no prompt is made from nothing. */
	private int freeSlots()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return -1;

		return InventoryCheck.freeSlots(inventoryIds());
	}

	/** The item id of the worn amulet, or {@code -1} when none is worn. */
	private int wornAmulet()
	{
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);
		Item amulet = worn == null ? null : worn.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
		return amulet == null ? -1 : amulet.getId();
	}

	/** The item ids in the backpack, {@code -1} for empty slots. */
	private int[] inventoryIds()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return new int[0];

		Item[] items = inventory.getItems();
		int[] ids = new int[items.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = items[i].getId();

		return ids;
	}

	/** How many vines in view are ripe now or about to be. */
	private int ripeSoon(int tick)
	{
		int soon = 0;
		for (Vine vine : tracker.all())
		{
			if (tracker.object(vine.getTile()) != null && vine.ticksUntilRipe(tick) <= WorldHopAdvisor.SOON_TICKS)
				soon++;
		}

		return soon;
	}

	/** How many other players stand within {@link #NEARBY_RADIUS} tiles of the local player. */
	private int nearbyPlayers(Player me)
	{
		WorldView view = client.getTopLevelWorldView();
		WorldPoint here = me.getWorldLocation();
		int nearby = 0;
		for (Player player : view.players())
		{
			if (player != me && player.getWorldLocation().distanceTo(here) <= NEARBY_RADIUS)
				nearby++;
		}

		return nearby;
	}
}
