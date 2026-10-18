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
import java.util.Collections;
import java.util.List;

/**
 * Pure planner that picks which vine to search next. It tries every order of the next few vines and keeps the one
 * that finishes the last search soonest, counting both the walking and any wait for a vine to grow back, so it
 * prefers a farther ripe vine to a nearer one that is still growing, and a vine that leaves the next one close by
 * to one that leaves it far away. Ties go to the order that walks least, then to the lower index. Works on plain
 * arrays so it is unit-testable without a client.
 */
final class VineRoute
{
	/** How many vines ahead the planner looks. */
	static final int DEPTH = 3;

	private VineRoute()
	{
	}

	/**
	 * Plans the next vines to search.
	 *
	 * @param startTicks ticks to walk from the player to beside each vine; {@link WalkDistance#UNREACHABLE} skips it
	 * @param between    {@code between[i][j]}: ticks to walk from beside vine i to beside vine j
	 * @param ripeAt     the tick each vine is, or is expected to be, ripe from
	 * @param now        the current game tick
	 * @param searchTicks ticks one search takes once at a vine
	 * @param depth      how many vines ahead to plan, at most
	 * @return the vine indexes in the order to search them; empty when none can be reached
	 */
	static List<Integer> plan(int[] startTicks, int[][] between, int[] ripeAt, int now, int searchTicks, int depth)
	{
		List<Integer> reachable = new ArrayList<>();
		for (int i = 0; i < startTicks.length; i++)
		{
			if (startTicks[i] != WalkDistance.UNREACHABLE)
				reachable.add(i);
		}

		int length = Math.min(depth, reachable.size());
		if (length <= 0)
			return Collections.emptyList();

		Search search = new Search(startTicks, between, ripeAt, searchTicks, reachable, length);
		search.extend(new int[length], 0, now, 0, -1);
		List<Integer> best = new ArrayList<>();
		for (int index : search.bestOrder)
			best.add(index);

		return best;
	}

	/** One depth-first search over visit orders, remembering the best order found so far. */
	private static final class Search
	{
		private final int[] startTicks;
		private final int[][] between;
		private final int[] ripeAt;
		private final int searchTicks;
		private final List<Integer> reachable;
		private final int length;
		private final boolean[] used;
		private long bestFinish = Long.MAX_VALUE;
		private long bestWalk = Long.MAX_VALUE;
		private int[] bestOrder = new int[0];

		Search(int[] startTicks, int[][] between, int[] ripeAt, int searchTicks, List<Integer> reachable, int length)
		{
			this.startTicks = startTicks;
			this.between = between;
			this.ripeAt = ripeAt;
			this.searchTicks = searchTicks;
			this.reachable = reachable;
			this.length = length;
			this.used = new boolean[startTicks.length];
		}

		/** Adds each unused vine as the next in the order, and records the order when it is full. */
		void extend(int[] order, int placed, long time, long walked, int last)
		{
			if (placed == length)
			{
				if (time < bestFinish || (time == bestFinish && walked < bestWalk))
				{
					bestFinish = time;
					bestWalk = walked;
					bestOrder = order.clone();
				}

				return;
			}

			for (int vine : reachable)
			{
				int walk = last < 0 ? startTicks[vine] : between[last][vine];
				if (used[vine] || walk == WalkDistance.UNREACHABLE)
					continue;

				long start = Math.max(time + walk, ripeAt[vine]);
				used[vine] = true;
				order[placed] = vine;
				extend(order, placed + 1, start + searchTicks, walked + walk, vine);
				used[vine] = false;
			}
		}
	}
}
