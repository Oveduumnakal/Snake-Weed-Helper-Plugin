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
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Singleton;

import net.runelite.api.coords.WorldPoint;

/**
 * The tiles of every vine ever seen, kept between sessions so the way back to the vines can be routed from the bank,
 * where none of them is in the scene. The set only grows; a vine never moves. Saved as {@code x,y,plane} triples
 * separated by semicolons.
 */
@Singleton
class KnownVines
{
	private final Set<WorldPoint> tiles = new LinkedHashSet<>();
	private boolean changed;

	/** Remembers a vine's tile, and notes that the set needs saving if the tile is new. */
	void add(WorldPoint tile)
	{
		if (tiles.add(tile))
			changed = true;
	}

	/** Every remembered tile. */
	Set<WorldPoint> tiles()
	{
		return Collections.unmodifiableSet(tiles);
	}

	/** Whether any remembered vine is within a number of tiles of a point, on the same plane. */
	boolean isNear(WorldPoint point, int radius)
	{
		for (WorldPoint tile : tiles)
		{
			if (tile.getPlane() == point.getPlane() && tile.distanceTo2D(point) <= radius)
				return true;
		}

		return false;
	}

	/** Whether the set grew since it was last saved; reading it marks it saved. */
	boolean takeChanged()
	{
		boolean was = changed;
		changed = false;
		return was;
	}

	/** Replaces the set with a saved one. */
	void load(String saved)
	{
		tiles.clear();
		tiles.addAll(decode(saved));
		changed = false;
	}

	/** The set as saved text. */
	String save()
	{
		return encode(tiles);
	}

	/**
	 * Writes tiles as text.
	 *
	 * @param tiles the tiles
	 * @return {@code x,y,plane} triples separated by semicolons; empty for no tiles
	 */
	static String encode(Collection<WorldPoint> tiles)
	{
		StringBuilder text = new StringBuilder();
		for (WorldPoint tile : tiles)
		{
			if (text.length() > 0)
				text.append(';');

			text.append(tile.getX()).append(',');
			text.append(tile.getY()).append(',');
			text.append(tile.getPlane());
		}

		return text.toString();
	}

	/**
	 * Reads tiles from text, skipping anything malformed.
	 *
	 * @param text the saved text, or {@code null}
	 * @return the tiles
	 */
	static Set<WorldPoint> decode(String text)
	{
		Set<WorldPoint> tiles = new LinkedHashSet<>();
		if (text == null || text.trim().isEmpty())
			return tiles;

		for (String entry : text.split(";"))
		{
			String[] parts = entry.trim().split(",");
			if (parts.length != 3)
				continue;

			try
			{
				tiles.add(new WorldPoint(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2])));
			}
			catch (NumberFormatException e)
			{
				continue;
			}
		}

		return tiles;
	}
}
