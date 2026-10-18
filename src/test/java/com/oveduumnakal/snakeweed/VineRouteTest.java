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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests {@link VineRoute}: ripeness against distance, lookahead, and unreachable vines. */
public class VineRouteTest
{
	private static final int UNREACHABLE = WalkDistance.UNREACHABLE;

	@Test
	public void nothingReachableGivesNoPlan()
	{
		int[] start = {UNREACHABLE, UNREACHABLE};
		int[][] between = {{0, 1}, {1, 0}};

		assertTrue(VineRoute.plan(start, between, new int[]{0, 0}, 0, 3, 3).isEmpty());
		assertTrue(VineRoute.plan(new int[0], new int[0][0], new int[0], 0, 3, 3).isEmpty());
	}

	@Test
	public void prefersAFartherRipeVineToANearerGrowingOne()
	{
		int[] start = {1, 5};
		int[][] between = {{0, 4}, {4, 0}};
		int[] ripeAt = {50, 0};

		assertEquals(Collections.singletonList(1), VineRoute.plan(start, between, ripeAt, 0, 3, 1));
	}

	@Test
	public void lookaheadBeatsNearestFirst()
	{
		int[] start = {2, 3, 3, 3};
		int[][] between = {
			{0, 10, 10, 10},
			{10, 0, 1, 1},
			{10, 1, 0, 1},
			{10, 1, 1, 0}
		};
		int[] ripeAt = {0, 0, 0, 0};

		assertEquals(Arrays.asList(1, 2, 3), VineRoute.plan(start, between, ripeAt, 0, 3, 3));
	}

	@Test
	public void skipsAnUnreachableVine()
	{
		int[] start = {UNREACHABLE, 4};
		int[][] between = {{0, 1}, {1, 0}};

		assertEquals(Collections.singletonList(1), VineRoute.plan(start, between, new int[]{0, 0}, 0, 3, 3));
	}

	@Test
	public void whenNothingIsRipeItTakesTheNearerAndWaits()
	{
		int[] start = {2, 4};
		int[][] between = {{0, 3}, {3, 0}};
		int[] ripeAt = {30, 30};

		assertEquals(Collections.singletonList(0), VineRoute.plan(start, between, ripeAt, 0, 3, 1));
	}

	@Test
	public void planIsNoLongerThanTheVinesThatCanBeReached()
	{
		int[] start = {1, 2};
		int[][] between = {{0, 1}, {1, 0}};

		assertEquals(Arrays.asList(0, 1), VineRoute.plan(start, between, new int[]{0, 0}, 0, 3, 3));
	}

	@Test
	public void aRipeningVineIsWorthWaitingForOnlyIfItIsSoonEnough()
	{
		int[] start = {1, 6};
		int[][] between = {{0, 6}, {6, 0}};

		assertEquals(Collections.singletonList(0), VineRoute.plan(start, between, new int[]{4, 0}, 0, 3, 1));
		assertEquals(Collections.singletonList(1), VineRoute.plan(start, between, new int[]{40, 0}, 0, 3, 1));
	}
}
