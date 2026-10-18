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

import net.runelite.api.coords.WorldPoint;

/** One marshy jungle vine as last seen: where it is, whether it can be searched, and when it will be ripe. */
final class Vine
{
	private final WorldPoint tile;
	private final VineState state;
	private final int ripeAtTick;

	Vine(WorldPoint tile, VineState state, int ripeAtTick)
	{
		this.tile = tile;
		this.state = state;
		this.ripeAtTick = ripeAtTick;
	}

	/** The vine's tile. */
	WorldPoint getTile()
	{
		return tile;
	}

	/** What is known about whether the vine can be searched. */
	VineState getState()
	{
		return state;
	}

	/** The game tick the vine is, or was, expected to be ripe from. */
	int getRipeAtTick()
	{
		return ripeAtTick;
	}

	/** Whether the vine can be searched right now. */
	boolean isSearchable()
	{
		return state == VineState.RIPE;
	}

	/**
	 * How many ticks until the vine is expected to be ripe.
	 *
	 * @param tick the current game tick
	 * @return 0 for a ripe vine or one whose time has passed, else the ticks left
	 */
	int ticksUntilRipe(int tick)
	{
		if (state == VineState.RIPE)
			return 0;

		return Math.max(0, ripeAtTick - tick);
	}
}
