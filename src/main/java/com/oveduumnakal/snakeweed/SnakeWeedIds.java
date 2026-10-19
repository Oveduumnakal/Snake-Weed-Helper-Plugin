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

import net.runelite.api.gameval.ObjectID;

/** Object ids and timings for the marshy jungle vines. */
final class SnakeWeedIds
{
	/** A ripe vine, which can be searched. */
	static final int VINE_RIPE = ObjectID.SNAKE_VINE_FULL;

	/** A picked vine, which is growing back. */
	static final int VINE_PICKED = ObjectID.SNAKE_VINE_EMPTY;

	/** Ticks a picked vine takes to grow back: 60 seconds (wiki; Phase 1 confirms it in game). */
	static final int REGROW_TICKS = 100;

	/** Ticks a vine first seen picked is guessed to need: half the regrow time, as the picking was not seen. */
	static final int UNKNOWN_RIPE_TICKS = REGROW_TICKS / 2;

	/** Milliseconds in one game tick, for showing tick counts as a countdown. */
	static final int TICK_MILLIS = 600;

	private SnakeWeedIds()
	{
	}

	/**
	 * Whether an object id is a vine, ripe or picked.
	 *
	 * @param id the object id
	 * @return true for a vine
	 */
	static boolean isVine(int id)
	{
		return id == VINE_RIPE || id == VINE_PICKED;
	}
}
