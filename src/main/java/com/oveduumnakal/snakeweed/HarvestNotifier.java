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
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;

/** Sends the opt-in notification when a herb lands in the backpack, which is when a search is complete. */
@Singleton
class HarvestNotifier
{
	/** Text of the notification. */
	static final String MESSAGE = "Snake weed harvested";

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final Notifier notifier;
	private final HarvestWatch watch = new HarvestWatch();

	@Inject
	HarvestNotifier(Client client, SnakeWeedHelperConfig config, Notifier notifier)
	{
		this.client = client;
		this.config = config;
		this.notifier = notifier;
	}

	/**
	 * Checks a changed item container for a harvested herb. Only the backpack counts.
	 *
	 * @param containerId the id of the container that changed
	 * @param container   the container
	 */
	void onItemsChanged(int containerId, ItemContainer container)
	{
		if (containerId != InventoryID.INV)
			return;

		if (watch.update(container.count(ItemID.UNIDENTIFIED_SNAKE_WEED), bankOpen()))
			notifier.notify(config.notifyHarvested(), MESSAGE);
	}

	/** Forgets the herb count, for logout or when the plugin stops. */
	void reset()
	{
		watch.reset();
	}

	/** Whether the bank interface is showing. */
	private boolean bankOpen()
	{
		Widget items = client.getWidget(InterfaceID.Bankmain.ITEMS);
		return items != null && !items.isHidden();
	}
}
