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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The real walk from every tile a vine can be searched from to every vine, so the planner can price each leg from
 * the tile the player would actually be standing on, not from some average of a vine's sides. A vine is searched
 * from a tile touching it; walking to the next vine ends on whichever side of it is nearest, and that tile is where
 * the leg after starts. The table is built once per set of vines and scene; each tick only the row for the player's
 * own tile is added. Works on plain arrays so it is unit-testable without a client.
 */
final class WalkTable
{
	private final WalkDistance walk;
	private final List<int[]> sites;
	private final Map<Long, Integer> standingIndex = new HashMap<>();
	private final int[][] cost;
	private final int[][] arrive;

	/**
	 * Measures the walk from beside each vine to beside every other.
	 *
	 * @param walk  the walking distances over the scene
	 * @param sites the vines as {@code {x, y}} scene tiles
	 */
	WalkTable(WalkDistance walk, List<int[]> sites)
	{
		this.walk = walk;
		this.sites = sites;
		List<int[]> standing = new ArrayList<>();
		for (int[] site : sites)
		{
			for (int[] side : walk.sides(site[0], site[1]))
			{
				if (!standingIndex.containsKey(key(side)))
				{
					standingIndex.put(key(side), standing.size());
					standing.add(side);
				}
			}
		}

		cost = new int[standing.size()][];
		arrive = new int[standing.size()][];
		for (int t = 0; t < cost.length; t++)
			fill(cost, arrive, t, walk.from(standing.get(t)[0], standing.get(t)[1]));
	}

	/**
	 * The legs the planner needs for one tick: every standing tile's row, plus a last row for the player.
	 *
	 * @param fromPlayer the steps from the player's tile to every tile, from {@link WalkDistance#from}
	 * @return the legs, whose {@link Legs#start} is the player's row
	 */
	Legs legsFrom(int[][] fromPlayer)
	{
		int rows = cost.length;
		int[][] allCost = new int[rows + 1][];
		int[][] allArrive = new int[rows + 1][];
		System.arraycopy(cost, 0, allCost, 0, rows);
		System.arraycopy(arrive, 0, allArrive, 0, rows);
		fill(allCost, allArrive, rows, fromPlayer);
		return new Legs(allCost, allArrive, rows);
	}

	/** Fills one row: the ticks to touch each vine from a tile, and the side of it that is reached. */
	private void fill(int[][] costs, int[][] arrivals, int row, int[][] distance)
	{
		costs[row] = new int[sites.size()];
		arrivals[row] = new int[sites.size()];
		for (int j = 0; j < sites.size(); j++)
		{
			int[] side = walk.approachTile(distance, sites.get(j)[0], sites.get(j)[1]);
			costs[row][j] = side == null ? WalkDistance.UNREACHABLE : WalkDistance.ticks(distance[side[0]][side[1]]);
			arrivals[row][j] = side == null ? -1 : standingIndex.get(key(side));
		}
	}

	private static long key(int[] tile)
	{
		return ((long) tile[0] << 32) | (tile[1] & 0xffffffffL);
	}

	/** The ticks and arrival tiles of every leg the planner may take, with the row it starts from. */
	static final class Legs
	{
		private final int[][] cost;
		private final int[][] arrive;
		private final int start;

		Legs(int[][] cost, int[][] arrive, int start)
		{
			this.cost = cost;
			this.arrive = arrive;
			this.start = start;
		}

		/** {@code cost()[tile][vine]}: ticks to walk from a standing tile to touch a vine. */
		int[][] cost()
		{
			return cost;
		}

		/** {@code arrive()[tile][vine]}: the standing tile reached beside the vine, or -1 when it cannot be reached. */
		int[][] arrive()
		{
			return arrive;
		}

		/** The row of the player's own tile. */
		int start()
		{
			return start;
		}
	}
}
