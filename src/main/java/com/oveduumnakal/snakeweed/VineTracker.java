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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Singleton;

import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

/**
 * Remembers every vine seen, and whether it can be searched. A vine seen changing while in view is timed exactly; a
 * vine first seen already picked (login, a new scene, or being picked out of view) is only estimated. Vines stay
 * remembered after they leave the scene, so their timers keep counting while the player is at the bank.
 */
@Singleton
class VineTracker
{
	private final Map<WorldPoint, Vine> vines = new LinkedHashMap<>();
	private final Map<WorldPoint, GameObject> objects = new HashMap<>();

	/**
	 * Takes in a vine object that appeared in the scene, keeping it for drawing. Other objects are ignored.
	 *
	 * @param object the object that spawned
	 * @param tick   the current game tick
	 * @param live   whether the object changed while in view; false for objects loaded with a scene
	 */
	void track(GameObject object, int tick, boolean live)
	{
		if (!SnakeWeedIds.isVine(object.getId()))
			return;

		objects.put(object.getWorldLocation(), object);
		onSpawn(object.getId(), object.getWorldLocation(), tick, live);
	}

	/**
	 * Drops a vine object that left the scene. The vine's state is kept. The object is dropped only if it is still
	 * the one held for its tile, so a replacement that spawned first is not lost.
	 *
	 * @param object the object that despawned
	 */
	void untrack(GameObject object)
	{
		if (SnakeWeedIds.isVine(object.getId()))
			objects.remove(object.getWorldLocation(), object);
	}

	/**
	 * Updates a vine's state from a vine object appearing on its tile.
	 *
	 * @param objectId the vine object's id
	 * @param tile     the vine's tile
	 * @param tick     the current game tick
	 * @param live     whether the object changed while in view; false for objects loaded with a scene
	 */
	void onSpawn(int objectId, WorldPoint tile, int tick, boolean live)
	{
		if (objectId == SnakeWeedIds.VINE_RIPE)
		{
			vines.put(tile, new Vine(tile, VineState.RIPE, tick));
			return;
		}

		if (objectId != SnakeWeedIds.VINE_PICKED)
			return;

		if (live)
		{
			vines.put(tile, new Vine(tile, VineState.PICKED, tick + SnakeWeedIds.REGROW_TICKS));
			return;
		}

		Vine known = vines.get(tile);
		if (known != null && known.getState() != VineState.RIPE && known.getRipeAtTick() > tick)
			return;

		vines.put(tile, new Vine(tile, VineState.UNKNOWN, tick + SnakeWeedIds.UNKNOWN_RIPE_TICKS));
	}

	/** The vine on a tile, or {@code null} when none has been seen there. */
	Vine get(WorldPoint tile)
	{
		return vines.get(tile);
	}

	/** Every vine seen, in the order first seen. */
	Collection<Vine> all()
	{
		return Collections.unmodifiableCollection(vines.values());
	}

	/** The scene object currently drawn for a vine, or {@code null} when it is out of the scene. */
	GameObject object(WorldPoint tile)
	{
		return objects.get(tile);
	}

	/** Forgets everything, for a new login or when the plugin stops. */
	void clear()
	{
		vines.clear();
		objects.clear();
	}
}
