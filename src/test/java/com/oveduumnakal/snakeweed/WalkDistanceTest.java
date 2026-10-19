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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Tests {@link WalkDistance} on hand-built collision grids: open ground, walls, blocked corners and objects. */
public class WalkDistanceTest
{
	private static final int SIZE = 14;

	private static int[][] open()
	{
		return new int[SIZE][SIZE];
	}

	@Test
	public void openGroundDistanceIsTheLongerAxis()
	{
		int[][] distance = new WalkDistance(open()).from(0, 0);

		assertEquals(0, distance[0][0]);
		assertEquals(5, distance[5][5]);
		assertEquals(10, distance[10][3]);
	}

	@Test
	public void fullWallForcesTheLongWayRound()
	{
		int[][] flags = open();
		for (int y = 0; y < SIZE - 1; y++)
			flags[5][y] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

		int[][] distance = new WalkDistance(flags).from(0, 0);

		assertTrue(distance[10][0] > 10);
		assertTrue(distance[10][0] != WalkDistance.UNREACHABLE);
	}

	@Test
	public void closedWallMakesTheFarSideUnreachable()
	{
		int[][] flags = open();
		for (int y = 0; y < SIZE; y++)
			flags[5][y] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

		assertEquals(WalkDistance.UNREACHABLE, new WalkDistance(flags).from(0, 0)[10][0]);
	}

	@Test
	public void thinWallBlocksBothDirections()
	{
		int[][] flags = open();
		for (int y = 0; y <= 3; y++)
		{
			flags[2][y] |= CollisionDataFlag.BLOCK_MOVEMENT_EAST;
			flags[3][y] |= CollisionDataFlag.BLOCK_MOVEMENT_WEST;
		}

		WalkDistance walk = new WalkDistance(flags);

		assertTrue(walk.from(0, 0)[4][0] > 4);
		assertTrue(walk.from(4, 0)[0][0] > 4);
	}

	@Test
	public void diagonalCannotCutABlockedCorner()
	{
		int[][] flags = open();
		flags[1][0] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

		assertEquals(2, new WalkDistance(flags).from(0, 0)[1][1]);
	}

	@Test
	public void standingBesideAnObjectTouchesIt()
	{
		int[][] flags = open();
		flags[5][5] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
		WalkDistance walk = new WalkDistance(flags);
		int[][] distance = walk.from(0, 5);

		assertEquals(4, walk.stepsToTouch(distance, 5, 5));
		assertArrayEquals(new int[]{4, 5}, walk.approachTile(distance, 5, 5));
	}

	@Test
	public void objectWithNoOpenSideCannotBeTouched()
	{
		int[][] flags = open();
		for (int dx = -1; dx <= 1; dx++)
		{
			for (int dy = -1; dy <= 1; dy++)
				flags[5 + dx][5 + dy] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
		}

		WalkDistance walk = new WalkDistance(flags);
		int[][] distance = walk.from(0, 0);

		assertEquals(WalkDistance.UNREACHABLE, walk.stepsToTouch(distance, 5, 5));
		assertNull(walk.approachTile(distance, 5, 5));
	}

	@Test
	public void runningCoversTwoTilesATick()
	{
		assertEquals(0, WalkDistance.ticks(0));
		assertEquals(1, WalkDistance.ticks(1));
		assertEquals(1, WalkDistance.ticks(2));
		assertEquals(2, WalkDistance.ticks(3));
		assertEquals(WalkDistance.UNREACHABLE, WalkDistance.ticks(WalkDistance.UNREACHABLE));
	}

	@Test
	public void ticksBetweenSitesIsSymmetricWithAZeroDiagonal()
	{
		List<int[]> sites = Arrays.asList(new int[]{2, 5}, new int[]{8, 5});
		int[][] ticks = new WalkDistance(open()).ticksBetween(sites);

		assertEquals(0, ticks[0][0]);
		assertEquals(0, ticks[1][1]);
		assertEquals(2, ticks[0][1]);
		assertEquals(2, ticks[1][0]);
	}

	@Test
	public void ticksFromThePlayerCountToTheNearestSide()
	{
		List<int[]> sites = Arrays.asList(new int[]{5, 0}, new int[]{12, 12});
		int[] ticks = new WalkDistance(open()).ticksFrom(0, 0, sites);

		assertEquals(2, ticks[0]);
		assertEquals(6, ticks[1]);
	}
}
