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
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import net.runelite.api.coords.WorldPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link KnownVines}: remembering vine tiles, saving them, and finding whether the player is near any. */
public class KnownVinesTest
{
	private static final WorldPoint A = new WorldPoint(2800, 3100, 0);
	private static final WorldPoint B = new WorldPoint(2805, 3102, 0);

	@Test
	public void saveAndLoadRoundTrip()
	{
		KnownVines vines = new KnownVines();
		vines.add(A);
		vines.add(B);

		KnownVines loaded = new KnownVines();
		loaded.load(vines.save());

		assertEquals(new LinkedHashSet<>(Arrays.asList(A, B)), loaded.tiles());
	}

	@Test
	public void addingAKnownTileIsNotAChange()
	{
		KnownVines vines = new KnownVines();
		vines.add(A);

		assertTrue(vines.takeChanged());
		assertFalse(vines.takeChanged());

		vines.add(A);

		assertFalse(vines.takeChanged());

		vines.add(B);

		assertTrue(vines.takeChanged());
	}

	@Test
	public void loadingIsNotAChange()
	{
		KnownVines vines = new KnownVines();
		vines.add(A);
		vines.load("2800,3100,0");

		assertFalse(vines.takeChanged());
	}

	@Test
	public void decodeSkipsJunk()
	{
		Set<WorldPoint> tiles = KnownVines.decode("2800,3100,0;nonsense;1,2;a,b,c;2805,3102,0;");

		assertEquals(new LinkedHashSet<>(Arrays.asList(A, B)), tiles);
		assertTrue(KnownVines.decode(null).isEmpty());
		assertTrue(KnownVines.decode("  ").isEmpty());
	}

	@Test
	public void nearMeansWithinRadiusOnTheSamePlane()
	{
		KnownVines vines = new KnownVines();
		vines.add(A);

		assertTrue(vines.isNear(new WorldPoint(2810, 3095, 0), 12));
		assertFalse(vines.isNear(new WorldPoint(2830, 3100, 0), 12));
		assertFalse(vines.isNear(new WorldPoint(2800, 3100, 1), 12));
	}

	@Test
	public void encodeOfNothingIsEmpty()
	{
		assertEquals("", KnownVines.encode(new LinkedHashSet<WorldPoint>()));
	}
}
