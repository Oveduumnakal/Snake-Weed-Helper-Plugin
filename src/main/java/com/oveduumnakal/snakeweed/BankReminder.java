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
import net.runelite.api.Player;
import net.runelite.api.gameval.InventoryID;

/**
 * Knows when the backpack is full at the vines, which is when the vine highlights go off and the tile under the
 * player is marked "Time to Bank". Checked every tick. The marker only shows near the vines, so it is gone after a
 * walk out of range or a teleport; the highlights stay off while the backpack is full, since nothing can be
 * searched then.
 */
@Singleton
class BankReminder
{
	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;

	private boolean full;
	private boolean atVines;

	@Inject
	BankReminder(Client client, SnakeWeedHelperConfig config, VineTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.tracker = tracker;
	}

	/** Whether the vine highlights and the next-vine ring should be off, because the backpack is full. */
	boolean hidesHighlights()
	{
		return config.bankMarker() && full;
	}

	/** Whether the "Time to Bank" tile should be drawn under the player. */
	boolean showsMarker()
	{
		return hidesHighlights() && atVines;
	}

	/** Forgets the state, for logout or when the plugin stops. */
	void reset()
	{
		full = false;
		atVines = false;
	}

	/** Rechecks the backpack and where the player is. */
	void update()
	{
		Player me = client.getLocalPlayer();
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		full = inventory != null && InventoryCheck.freeSlots(InventoryCheck.ids(inventory)) == 0;
		atVines = me != null && tracker.isNear(me.getWorldLocation(), VineTracker.NEAR_VINES);
	}
}
