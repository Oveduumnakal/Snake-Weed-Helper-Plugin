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

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests {@link TickClock}: time into the current tick, reset on each tick and held at a tick's length. */
public class TickClockTest
{
	@Test
	public void countsUpFromTheLastTick()
	{
		AtomicLong now = new AtomicLong(1000);
		TickClock clock = new TickClock(now::get);
		clock.onTick();
		now.set(1250);

		assertEquals(250, clock.millisSinceTick());
	}

	@Test
	public void aNewTickStartsOverAtZero()
	{
		AtomicLong now = new AtomicLong(1000);
		TickClock clock = new TickClock(now::get);
		now.set(1500);
		clock.onTick();

		assertEquals(0, clock.millisSinceTick());
	}

	@Test
	public void aLateTickHoldsAtTheTickLength()
	{
		AtomicLong now = new AtomicLong(1000);
		TickClock clock = new TickClock(now::get);
		clock.onTick();
		now.set(2500);

		assertEquals(SnakeWeedIds.TICK_MILLIS, clock.millisSinceTick());
	}

	@Test
	public void aClockThatStepsBackIsHeldAtZero()
	{
		AtomicLong now = new AtomicLong(1000);
		TickClock clock = new TickClock(now::get);
		clock.onTick();
		now.set(900);

		assertEquals(0, clock.millisSinceTick());
	}
}
