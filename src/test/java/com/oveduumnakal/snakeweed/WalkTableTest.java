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
import java.util.List;

import org.junit.Test;

import net.runelite.api.CollisionDataFlag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests {@link WalkTable}: legs priced by real walking from the tile the player stands on, around walls. */
public class WalkTableTest
{
	private static final int SIZE = 30;

	private static int[][] open()
	{
		return new int[SIZE][SIZE];
	}

	@Test
	public void theFirstLegIsPricedFromThePlayersOwnTile()
	{
		List<int[]> sites = Arrays.asList(new int[]{5, 5}, new int[]{20, 5});
		WalkDistance walk = new WalkDistance(open());
		WalkTable.Legs legs = new WalkTable(walk, sites).legsFrom(walk.from(4, 5));

		int[] fromPlayer = legs.cost()[legs.start()];
		assertEquals(0, fromPlayer[0]);
		assertEquals(WalkDistance.ticks(15), fromPlayer[1]);
	}

	@Test
	public void aWallBetweenVinesCountsAsTheWalkAroundItNotTheTilesBetween()
	{
		int[][] flags = open();
		for (int y = 0; y < SIZE - 1; y++)
			flags[12][y] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

		List<int[]> sites = Arrays.asList(new int[]{10, 5}, new int[]{14, 5});
		WalkDistance walk = new WalkDistance(flags);
		WalkTable.Legs legs = new WalkTable(walk, sites).legsFrom(walk.from(9, 5));

		assertTrue("round the wall, not the 4 tiles between", legs.cost()[legs.start()][1] > WalkDistance.ticks(20));
	}

	@Test
	public void theLegAfterAVineStartsFromTheSideTheWalkArrivedAt()
	{
		List<int[]> sites = Arrays.asList(new int[]{10, 10}, new int[]{20, 10});
		WalkDistance walk = new WalkDistance(open());
		WalkTable.Legs legs = new WalkTable(walk, sites).legsFrom(walk.from(2, 10));

		int[] arrivalTiles = legs.arrive()[legs.start()];
		int sideOfFirst = arrivalTiles[0];
		assertEquals(WalkDistance.ticks(7), legs.cost()[legs.start()][0]);
		assertEquals(WalkDistance.ticks(10), legs.cost()[sideOfFirst][1]);
	}

	@Test
	public void aVineWithNoOpenSideCannotBeReached()
	{
		int[][] flags = open();
		for (int dx = -1; dx <= 1; dx++)
		{
			for (int dy = -1; dy <= 1; dy++)
			{
				if (dx != 0 || dy != 0)
					flags[10 + dx][10 + dy] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
			}
		}

		List<int[]> sites = Arrays.asList(new int[]{10, 10}, new int[]{20, 10});
		WalkDistance walk = new WalkDistance(flags);
		WalkTable.Legs legs = new WalkTable(walk, sites).legsFrom(walk.from(2, 10));

		assertEquals(WalkDistance.UNREACHABLE, legs.cost()[legs.start()][0]);
		assertEquals(-1, legs.arrive()[legs.start()][0]);
	}
}
