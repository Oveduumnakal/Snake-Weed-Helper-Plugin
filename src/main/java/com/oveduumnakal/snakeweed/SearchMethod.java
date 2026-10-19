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

/**
 * How the player searches the vines, which sets how long the planner expects one vine to take. A normal search rolls
 * every 10 to 11 ticks and averaged 1.5 rolls a herb over 25 searches logged in game, so about 16 ticks a vine. The
 * 3-tick figure is an estimate (a roll every 3 ticks at the same odds, plus the click) until it has been logged.
 */
public enum SearchMethod
{
	STANDARD("Standard (about 16 ticks)", 16),
	THREE_TICK("3-tick mahogany (about 5 ticks)", 5);

	private final String label;
	private final int expectedTicks;

	SearchMethod(String label, int expectedTicks)
	{
		this.label = label;
		this.expectedTicks = expectedTicks;
	}

	/** Game ticks one vine is expected to take once the player is beside it, from the first roll to the herb. */
	int expectedTicks()
	{
		return expectedTicks;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
