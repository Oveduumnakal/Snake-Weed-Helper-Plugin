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

import net.runelite.api.gameval.ItemID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link GloryHint}: only a full backpack and a charged glory prompt a teleport. */
public class GloryHintTest
{
	@Test
	public void aFullBackpackAndAChargedGloryPrompts()
	{
		assertTrue(GloryHint.shouldPrompt(0, ItemID.AMULET_OF_GLORY_4));
		assertTrue(GloryHint.shouldPrompt(0, ItemID.AMULET_OF_GLORY_1));
		assertTrue(GloryHint.shouldPrompt(0, ItemID.AMULET_OF_GLORY_6));
	}

	@Test
	public void trimmedAndEternalGloriesCount()
	{
		assertTrue(GloryHint.shouldPrompt(0, ItemID.TRAIL_AMULET_OF_GLORY_3));
		assertTrue(GloryHint.shouldPrompt(0, ItemID.AMULET_OF_GLORY_INF));
	}

	@Test
	public void anUnchargedGloryDoesNot()
	{
		assertFalse(GloryHint.shouldPrompt(0, ItemID.AMULET_OF_GLORY));
		assertFalse(GloryHint.shouldPrompt(0, ItemID.TRAIL_AMULET_OF_GLORY));
	}

	@Test
	public void noAmuletOrAnotherAmuletDoesNot()
	{
		assertFalse(GloryHint.shouldPrompt(0, -1));
		assertFalse(GloryHint.shouldPrompt(0, ItemID.KNIFE));
	}

	@Test
	public void roomLeftInTheBackpackDoesNot()
	{
		assertFalse(GloryHint.shouldPrompt(1, ItemID.AMULET_OF_GLORY_4));
		assertFalse(GloryHint.shouldPrompt(-1, ItemID.AMULET_OF_GLORY_4));
	}
}
