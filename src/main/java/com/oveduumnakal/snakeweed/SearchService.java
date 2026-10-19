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

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;

/**
 * Feeds {@link SearchTracker} from the game: the clicks that start a 3-tick search, the fletch animation, and herbs
 * arriving in the backpack. It also leaves a line in the tick log whenever the prompt changes, so a session can be
 * checked against what the game did.
 */
@Singleton
class SearchService
{
	private final Client client;
	private final TickClock tickClock;
	private final TickLogger tickLogger;
	private final SearchTracker tracker = new SearchTracker();

	private int lastGrimy = -1;

	@Inject
	SearchService(Client client, TickClock tickClock, TickLogger tickLogger)
	{
		this.client = client;
		this.tickClock = tickClock;
		this.tickLogger = tickLogger;
	}

	/** The state of the search being followed. */
	SearchPhase phase()
	{
		return tracker.phase();
	}

	/** How many manual searches began because the click-away was missed. */
	int missed()
	{
		return tracker.missed();
	}

	/** Forgets the search being followed, for logout or when the plugin stops. */
	void reset()
	{
		tracker.reset();
		lastGrimy = -1;
	}

	/**
	 * Takes in a menu click, to see a 3-tick search being started.
	 *
	 * @param action   the clicked menu action
	 * @param itemId   the clicked item's id, when it was an item
	 * @param objectId the clicked object's id, when it was an object
	 */
	void onClick(MenuAction action, int itemId, int objectId)
	{
		int tick = client.getTickCount();
		if (action == MenuAction.WIDGET_TARGET_ON_WIDGET && (itemId == ItemID.MAHOGANY_LOGS || itemId == ItemID.KNIFE))
			tracker.logClicked(tick, tickClock.millisSinceTick());
		else if (action == MenuAction.GAME_OBJECT_SECOND_OPTION && SnakeWeedIds.isVine(objectId))
			tracker.vineSearched(tick);
	}

	/** Takes in the player's animation changing to the fletch animation. */
	void onFletchAnimation()
	{
		tracker.fletchAnimation(client.getTickCount());
	}

	/** Moves the search on one tick, noting any change of the prompt in the tick log. */
	void onTick()
	{
		Player me = client.getLocalPlayer();
		if (me == null)
			return;

		int grimy = grimyCount();
		boolean herb = lastGrimy >= 0 && grimy > lastGrimy;
		lastGrimy = grimy;
		SearchPhase before = tracker.phase();
		tracker.onTick(client.getTickCount(), me.getAnimation(), herb);
		if (tracker.phase() != before)
			tickLogger.note("PROMPT", before + " -> " + tracker.phase() + " missed=" + tracker.missed());
	}

	/** Grimy snake weed in the backpack, or the last count when the backpack is not loaded. */
	private int grimyCount()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		return inventory == null ? lastGrimy : inventory.count(ItemID.UNIDENTIFIED_SNAKE_WEED);
	}
}
