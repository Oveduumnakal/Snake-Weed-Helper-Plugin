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

import java.util.function.LongSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tells how far into the current game tick the client is, so a countdown can move smoothly between ticks instead of
 * jumping every 0.6 seconds. The plugin marks each tick as it arrives; the time since then is read by the overlay on
 * every frame. A tick that arrives late holds the countdown at the tick boundary rather than letting it run past.
 */
@Singleton
class TickClock
{
	private final LongSupplier clock;
	private long lastTickMillis;

	@Inject
	TickClock()
	{
		this(System::currentTimeMillis);
	}

	TickClock(LongSupplier clock)
	{
		this.clock = clock;
		this.lastTickMillis = clock.getAsLong();
	}

	/** Marks the start of a new game tick. */
	void onTick()
	{
		lastTickMillis = clock.getAsLong();
	}

	/**
	 * Milliseconds since the last tick began.
	 *
	 * @return 0 to {@link SnakeWeedIds#TICK_MILLIS}; a late tick does not push it past a tick's length
	 */
	long millisSinceTick()
	{
		long elapsed = clock.getAsLong() - lastTickMillis;
		return Math.max(0, Math.min(SnakeWeedIds.TICK_MILLIS, elapsed));
	}
}
