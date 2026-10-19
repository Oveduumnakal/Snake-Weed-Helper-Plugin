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

/** Tests {@link WorldHopAdvisor}: hop only when crowded and short of ripe vines. */
public class WorldHopAdvisorTest
{
	@Test
	public void crowdedAndShortOfRipeVinesSuggestsHopping()
	{
		assertTrue(WorldHopAdvisor.shouldHop(3, 1, 2, 4));
	}

	@Test
	public void aloneWithEveryVineGrowingIsJustAWait()
	{
		assertFalse(WorldHopAdvisor.shouldHop(0, 0, 2, 4));
	}

	@Test
	public void plentyOfRipeVinesMeansStay()
	{
		assertFalse(WorldHopAdvisor.shouldHop(5, 4, 2, 4));
	}

	@Test
	public void thresholdsAreInclusiveOnPlayersAndExclusiveOnRipeVines()
	{
		assertTrue(WorldHopAdvisor.shouldHop(2, 3, 2, 4));
		assertFalse(WorldHopAdvisor.shouldHop(1, 3, 2, 4));
		assertFalse(WorldHopAdvisor.shouldHop(2, 4, 2, 4));
	}
}
