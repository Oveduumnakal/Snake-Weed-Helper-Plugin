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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import net.runelite.api.CollisionDataFlag;

/**
 * Walking distances over a scene's collision flags, found by breadth-first search. Works on the plain
 * {@code flags[x][y]} grid the client gives, so it is unit-testable without a client. A step is one tile in any of
 * eight directions; a diagonal also needs both of its straight steps to be open, as in the game. The distance to a
 * vine is the distance to a tile beside it, since an object is searched from an adjacent tile.
 */
final class WalkDistance
{
	/** Distance of a tile that cannot be reached. */
	static final int UNREACHABLE = Integer.MAX_VALUE;

	private static final int[][] DIRECTIONS = {
		{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}
	};

	private final int[][] flags;
	private final int width;
	private final int height;

	WalkDistance(int[][] flags)
	{
		this.flags = flags;
		this.width = flags.length;
		this.height = flags.length == 0 ? 0 : flags[0].length;
	}

	/**
	 * Steps from one tile to every tile.
	 *
	 * @param x the start tile's x
	 * @param y the start tile's y
	 * @return the steps to each tile as {@code [x][y]}, {@link #UNREACHABLE} where it cannot be reached
	 */
	int[][] from(int x, int y)
	{
		List<int[]> sources = new ArrayList<>();
		sources.add(new int[]{x, y});
		return from(sources);
	}

	/**
	 * Steps from the nearest of several tiles to every tile; each source counts as 0.
	 *
	 * @param sources the start tiles as {@code {x, y}}
	 * @return the steps to each tile as {@code [x][y]}, {@link #UNREACHABLE} where it cannot be reached
	 */
	int[][] from(List<int[]> sources)
	{
		int[][] distance = new int[width][height];
		for (int[] column : distance)
			Arrays.fill(column, UNREACHABLE);

		Deque<int[]> queue = new ArrayDeque<>();
		for (int[] source : sources)
		{
			if (inBounds(source[0], source[1]) && distance[source[0]][source[1]] == UNREACHABLE)
			{
				distance[source[0]][source[1]] = 0;
				queue.add(source);
			}
		}

		while (!queue.isEmpty())
		{
			int[] tile = queue.poll();
			for (int[] step : DIRECTIONS)
			{
				int nx = tile[0] + step[0];
				int ny = tile[1] + step[1];
				if (canStep(tile[0], tile[1], step[0], step[1]) && distance[nx][ny] == UNREACHABLE)
				{
					distance[nx][ny] = distance[tile[0]][tile[1]] + 1;
					queue.add(new int[]{nx, ny});
				}
			}
		}

		return distance;
	}

	/**
	 * Steps to stand beside an object: the fewest to any open tile touching it on a side.
	 *
	 * @param distance the steps to each tile, from {@link #from}
	 * @param x        the object's x
	 * @param y        the object's y
	 * @return the steps, or {@link #UNREACHABLE} when no side can be reached
	 */
	int stepsToTouch(int[][] distance, int x, int y)
	{
		int[] side = approachTile(distance, x, y);
		return side == null ? UNREACHABLE : distance[side[0]][side[1]];
	}

	/**
	 * The open tile beside an object that is fewest steps away.
	 *
	 * @param distance the steps to each tile, from {@link #from}
	 * @param x        the object's x
	 * @param y        the object's y
	 * @return the tile as {@code {x, y}}, or {@code null} when no side can be reached
	 */
	int[] approachTile(int[][] distance, int x, int y)
	{
		int[] best = null;
		for (int[] step : DIRECTIONS)
		{
			boolean straight = step[0] == 0 || step[1] == 0;
			int sx = x + step[0];
			int sy = y + step[1];
			if (straight && inBounds(sx, sy) && distance[sx][sy] != UNREACHABLE
				&& (best == null || distance[sx][sy] < distance[best[0]][best[1]]))
				best = new int[]{sx, sy};
		}

		return best;
	}

	/**
	 * Game ticks to walk some steps while running, two tiles a tick.
	 *
	 * @param steps the steps, or {@link #UNREACHABLE}
	 * @return the ticks, or {@link #UNREACHABLE}
	 */
	static int ticks(int steps)
	{
		return steps == UNREACHABLE ? UNREACHABLE : (steps + 1) / 2;
	}

	/**
	 * Ticks from a tile to stand beside each of several objects.
	 *
	 * @param x     the start tile's x
	 * @param y     the start tile's y
	 * @param sites the objects as {@code {x, y}}
	 * @return the ticks to each, {@link #UNREACHABLE} where it cannot be reached
	 */
	int[] ticksFrom(int x, int y, List<int[]> sites)
	{
		int[][] distance = from(x, y);
		int[] ticks = new int[sites.size()];
		for (int i = 0; i < ticks.length; i++)
			ticks[i] = ticks(stepsToTouch(distance, sites.get(i)[0], sites.get(i)[1]));

		return ticks;
	}

	/**
	 * Ticks to walk from beside each object to beside every other, for planning a visit order.
	 *
	 * @param sites the objects as {@code {x, y}}
	 * @return {@code ticks[i][j]}: from beside object i to beside object j; 0 on the diagonal
	 */
	int[][] ticksBetween(List<int[]> sites)
	{
		int[][] ticks = new int[sites.size()][sites.size()];
		for (int i = 0; i < ticks.length; i++)
		{
			int[][] distance = from(neighbours(sites.get(i)[0], sites.get(i)[1]));
			for (int j = 0; j < ticks.length; j++)
				ticks[i][j] = i == j ? 0 : ticks(stepsToTouch(distance, sites.get(j)[0], sites.get(j)[1]));
		}

		return ticks;
	}

	/** The open tiles touching an object on a side. */
	private List<int[]> neighbours(int x, int y)
	{
		List<int[]> open = new ArrayList<>();
		for (int[] step : DIRECTIONS)
		{
			boolean straight = step[0] == 0 || step[1] == 0;
			if (straight && passable(x + step[0], y + step[1]))
				open.add(new int[]{x + step[0], y + step[1]});
		}

		return open;
	}

	/** Whether a step is allowed: the tile is open, no wall is in the way, and a diagonal's straight steps are open. */
	private boolean canStep(int x, int y, int dx, int dy)
	{
		if (!passable(x + dx, y + dy) || (flags[x][y] & directionFlag(dx, dy)) != 0)
			return false;

		return dx == 0 || dy == 0 || (canStep(x, y, dx, 0) && canStep(x, y, 0, dy));
	}

	/** Whether a tile is inside the scene and not blocked by an object or the floor. */
	private boolean passable(int x, int y)
	{
		return inBounds(x, y) && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0;
	}

	private boolean inBounds(int x, int y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	/** The collision flag that blocks a step in a direction. */
	private static int directionFlag(int dx, int dy)
	{
		if (dx == 0)
			return dy > 0 ? CollisionDataFlag.BLOCK_MOVEMENT_NORTH : CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;

		if (dy == 0)
			return dx > 0 ? CollisionDataFlag.BLOCK_MOVEMENT_EAST : CollisionDataFlag.BLOCK_MOVEMENT_WEST;

		if (dx > 0)
			return dy > 0 ? CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST : CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST;

		return dy > 0 ? CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST : CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST;
	}
}
