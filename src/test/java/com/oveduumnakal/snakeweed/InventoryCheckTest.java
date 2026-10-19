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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import net.runelite.api.gameval.ItemID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests {@link InventoryCheck}: which of the knife and mahogany logs are reported missing. */
public class InventoryCheckTest
{
	@Test
	public void bothPresentMeansNothingMissing()
	{
		int[] items = {ItemID.KNIFE, -1, ItemID.MAHOGANY_LOGS};

		assertTrue(InventoryCheck.missing(items).isEmpty());
	}

	@Test
	public void emptyBackpackMissesBoth()
	{
		assertEquals(Arrays.asList(InventoryCheck.KNIFE, InventoryCheck.LOGS), InventoryCheck.missing(new int[28]));
	}

	@Test
	public void reportsOnlyTheKnifeWhenLogsAreCarried()
	{
		int[] items = {ItemID.MAHOGANY_LOGS};

		assertEquals(Collections.singletonList(InventoryCheck.KNIFE), InventoryCheck.missing(items));
	}

	@Test
	public void reportsOnlyTheLogsWhenTheKnifeIsCarried()
	{
		int[] items = {-1, ItemID.KNIFE};

		assertEquals(Collections.singletonList(InventoryCheck.LOGS), InventoryCheck.missing(items));
	}
}
