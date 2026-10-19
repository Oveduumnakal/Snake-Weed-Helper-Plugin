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

import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;

/**
 * Remembers the vine the player has clicked to search, from the click until the herb is found or the player does
 * something else. While a vine is committed the planner prices the vines after it from beside it, so the marker shows
 * where to go once this search ends instead of sitting on the vine being searched.
 */
final class SearchCommitment
{
	/** Ticks on the same tile away from the vine, after which the click is taken as having come to nothing. */
	static final int STILL_TICKS = 3;

	private WorldPoint vine;
	private WorldPoint lastPosition;
	private int stillTicks;

	/**
	 * Takes in a menu click.
	 *
	 * @param action   the clicked menu action
	 * @param objectId the clicked object's id, when it was an object
	 * @param tile     the clicked object's tile, when it was an object
	 */
	void clicked(MenuAction action, int objectId, WorldPoint tile)
	{
		if (action == MenuAction.GAME_OBJECT_SECOND_OPTION && SnakeWeedIds.isVine(objectId) && tile != null)
		{
			vine = tile;
			lastPosition = null;
			stillTicks = 0;
			return;
		}

		if (abandons(action))
			vine = null;
	}

	/**
	 * The vine being searched or walked to, once per tick.
	 *
	 * @param player     where the player stands
	 * @param searchable whether that vine can still be searched, which it cannot once the herb is found
	 * @return the vine, or {@code null} when none is committed any more
	 */
	WorldPoint update(WorldPoint player, boolean searchable)
	{
		if (vine == null)
			return null;

		stillTicks = player.equals(lastPosition) ? stillTicks + 1 : 1;
		lastPosition = player;
		if (!searchable || (stillTicks >= STILL_TICKS && !beside(player, vine)))
			vine = null;

		return vine;
	}

	/** The committed vine as of the last update, without ageing it. */
	WorldPoint vine()
	{
		return vine;
	}

	/** Forgets the vine, for logout or when the plugin stops. */
	void clear()
	{
		vine = null;
		lastPosition = null;
		stillTicks = 0;
	}

	/**
	 * Whether a click is the player turning to something else: walking off or using another object, NPC or item.
	 *
	 * @param action the clicked menu action
	 * @return true when it replaces whatever the player was doing
	 */
	static boolean abandons(MenuAction action)
	{
		switch (action)
		{
			case WALK:
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
			case GROUND_ITEM_FIRST_OPTION:
			case GROUND_ITEM_SECOND_OPTION:
			case GROUND_ITEM_THIRD_OPTION:
			case GROUND_ITEM_FOURTH_OPTION:
			case GROUND_ITEM_FIFTH_OPTION:
				return true;
			default:
				return false;
		}
	}

	/** Whether a tile touches another on a side. */
	private static boolean beside(WorldPoint a, WorldPoint b)
	{
		return a.getPlane() == b.getPlane() && Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) == 1;
	}
}
