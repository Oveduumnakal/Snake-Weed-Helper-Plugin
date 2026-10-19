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
import java.util.List;

import net.runelite.api.gameval.ItemID;

/**
 * Checks the backpack for what the 3-tick method needs, a knife and mahogany logs, so a missing item is caught
 * before the first search instead of mid-session. Pure and static.
 */
final class InventoryCheck
{
	/** Slots in the backpack. */
	static final int BACKPACK_SLOTS = 28;

	/** Name shown when no knife is carried. */
	static final String KNIFE = "knife";

	/** Name shown when no mahogany logs are carried. */
	static final String LOGS = "mahogany logs";

	private InventoryCheck()
	{
	}

	/**
	 * Empty backpack slots. The game's item list can end at the last item carried, so empty slots are counted as the
	 * backpack's size less the slots in use, not as the empty entries in the list.
	 *
	 * @param itemIds the inventory's item ids ({@code -1} for empty slots)
	 * @return the empty slots, 0 when the backpack is full
	 */
	static int freeSlots(int[] itemIds)
	{
		int used = 0;
		for (int id : itemIds)
		{
			if (id != -1)
				used++;
		}

		return Math.max(0, BACKPACK_SLOTS - used);
	}

	/**
	 * The needed items missing from an inventory.
	 *
	 * @param itemIds the inventory's item ids ({@code -1} for empty slots)
	 * @return the missing items' names in a fixed order; empty when nothing is missing
	 */
	static List<String> missing(int[] itemIds)
	{
		boolean knife = false;
		boolean logs = false;
		for (int id : itemIds)
		{
			if (id == ItemID.KNIFE)
				knife = true;
			else if (id == ItemID.MAHOGANY_LOGS)
				logs = true;
		}

		List<String> missing = new ArrayList<>();
		if (!knife)
			missing.add(KNIFE);

		if (!logs)
			missing.add(LOGS);

		return missing;
	}
}
