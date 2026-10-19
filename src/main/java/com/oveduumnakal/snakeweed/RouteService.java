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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Works out, each tick, which vine to search next. It reads the vines in the loaded scene from {@link VineTracker},
 * measures the walk to each over the scene's collision data, and hands the numbers to {@link VineRoute}. The walk
 * between vines only changes when the set of vines or the scene does, so that matrix is kept between ticks.
 */
@Singleton
class RouteService
{
	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;

	private WorldPoint nextVine;
	private List<WorldPoint> cachedSites = new ArrayList<>();
	private int cachedBaseX = -1;
	private int cachedBaseY = -1;
	private int[][] cachedBetween = new int[0][0];

	@Inject
	RouteService(Client client, SnakeWeedHelperConfig config, VineTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.tracker = tracker;
	}

	/** The tile of the vine to search next, or {@code null} when none can be reached. */
	WorldPoint getNextVine()
	{
		return nextVine;
	}

	/** Forgets the plan, for when the player leaves the vines. */
	void reset()
	{
		nextVine = null;
	}

	/**
	 * Re-plans from where the player stands.
	 *
	 * @param tick the current game tick
	 */
	void update(int tick)
	{
		reset();
		Player me = client.getLocalPlayer();
		WorldView view = client.getTopLevelWorldView();
		CollisionData[] maps = view == null ? null : view.getCollisionMaps();
		if (me == null || maps == null || view.getPlane() >= maps.length || maps[view.getPlane()] == null
			|| client.getGameState() != GameState.LOGGED_IN)
			return;

		List<Vine> vines = new ArrayList<>();
		List<int[]> sites = new ArrayList<>();
		for (Vine vine : tracker.all())
		{
			LocalPoint local = tracker.object(vine.getTile()) == null ? null
				: LocalPoint.fromWorld(view, vine.getTile());
			if (local != null)
			{
				vines.add(vine);
				sites.add(new int[]{local.getSceneX(), local.getSceneY()});
			}
		}

		if (vines.isEmpty())
			return;

		WalkDistance walk = new WalkDistance(maps[view.getPlane()].getFlags());
		LocalPoint mine = me.getLocalLocation();
		int[][] fromMe = walk.from(mine.getSceneX(), mine.getSceneY());
		int[] startTicks = new int[vines.size()];
		int[] ripeAt = new int[vines.size()];
		for (int i = 0; i < startTicks.length; i++)
		{
			startTicks[i] = WalkDistance.ticks(walk.stepsToTouch(fromMe, sites.get(i)[0], sites.get(i)[1]));
			ripeAt[i] = vines.get(i).getRipeAtTick();
		}

		List<Integer> plan = VineRoute.plan(startTicks, between(walk, vines, sites, view), ripeAt, tick,
			config.searchTicks(), VineRoute.DEPTH);
		if (plan.isEmpty())
			return;

		nextVine = vines.get(plan.get(0)).getTile();
	}

	/** The walk between vines, recomputed only when the vines or the scene have changed. */
	private int[][] between(WalkDistance walk, List<Vine> vines, List<int[]> sites, WorldView view)
	{
		List<WorldPoint> tiles = new ArrayList<>();
		for (Vine vine : vines)
			tiles.add(vine.getTile());

		if (!tiles.equals(cachedSites) || view.getBaseX() != cachedBaseX || view.getBaseY() != cachedBaseY)
		{
			cachedBetween = walk.ticksBetween(sites);
			cachedSites = tiles;
			cachedBaseX = view.getBaseX();
			cachedBaseY = view.getBaseY();
		}

		return cachedBetween;
	}
}
