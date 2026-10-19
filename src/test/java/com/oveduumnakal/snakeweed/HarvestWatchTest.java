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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link HarvestWatch}: which changes in the herb count are harvests. */
public class HarvestWatchTest
{
	@Test
	public void theFirstCountOnlySetsTheBaseline()
	{
		assertFalse(new HarvestWatch().update(5, false));
	}

	@Test
	public void aHigherCountIsAHarvest()
	{
		HarvestWatch watch = new HarvestWatch();
		watch.update(5, false);

		assertTrue(watch.update(6, false));
		assertFalse(watch.update(6, false));
	}

	@Test
	public void aLowerCountIsNotAHarvestButMovesTheBaseline()
	{
		HarvestWatch watch = new HarvestWatch();
		watch.update(5, false);

		assertFalse(watch.update(0, false));
		assertTrue(watch.update(1, false));
	}

	@Test
	public void withdrawingFromTheBankIsNotAHarvest()
	{
		HarvestWatch watch = new HarvestWatch();
		watch.update(0, true);

		assertFalse(watch.update(10, true));
		assertTrue(watch.update(11, false));
	}

	@Test
	public void resettingForgetsTheBaseline()
	{
		HarvestWatch watch = new HarvestWatch();
		watch.update(5, false);
		watch.reset();

		assertFalse(watch.update(9, false));
	}
}
