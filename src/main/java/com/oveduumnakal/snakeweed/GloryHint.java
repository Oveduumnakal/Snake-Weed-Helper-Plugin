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

import net.runelite.api.gameval.ItemID;

/**
 * Decides when to suggest an amulet of glory teleport: the backpack is full and a glory with charges is worn. Pure
 * and static.
 */
final class GloryHint
{
	/** Text of the prompt. */
	static final String TEXT = "Backpack full: teleport with your amulet of glory";

	private static final int[] CHARGED = {
		ItemID.AMULET_OF_GLORY_1, ItemID.AMULET_OF_GLORY_2, ItemID.AMULET_OF_GLORY_3, ItemID.AMULET_OF_GLORY_4,
		ItemID.AMULET_OF_GLORY_5, ItemID.AMULET_OF_GLORY_6, ItemID.AMULET_OF_GLORY_INF,
		ItemID.TRAIL_AMULET_OF_GLORY_1, ItemID.TRAIL_AMULET_OF_GLORY_2, ItemID.TRAIL_AMULET_OF_GLORY_3,
		ItemID.TRAIL_AMULET_OF_GLORY_4, ItemID.TRAIL_AMULET_OF_GLORY_5, ItemID.TRAIL_AMULET_OF_GLORY_6
	};

	private GloryHint()
	{
	}

	/**
	 * Whether to prompt a teleport.
	 *
	 * @param freeSlots empty backpack slots
	 * @param amuletId  the worn amulet's item id, or {@code -1} when none is worn
	 * @return true when the backpack is full and the amulet is a glory with charges left
	 */
	static boolean shouldPrompt(int freeSlots, int amuletId)
	{
		return freeSlots == 0 && isCharged(amuletId);
	}

	/**
	 * Whether an item is an amulet of glory that can still teleport: charged, trimmed or eternal. An uncharged one is
	 * not.
	 *
	 * @param itemId the item id
	 * @return true for a charged glory
	 */
	static boolean isCharged(int itemId)
	{
		for (int id : CHARGED)
		{
			if (id == itemId)
				return true;
		}

		return false;
	}
}
