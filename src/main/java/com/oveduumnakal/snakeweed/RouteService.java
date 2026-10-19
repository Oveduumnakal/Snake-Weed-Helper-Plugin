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
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Works out, each tick, which vine to search next. It reads the vines in the loaded scene from {@link VineTracker},
 * measures the walk to each from the tile the player stands on over the scene's collision data, and hands the
 * numbers to {@link VineRoute}. While the player is searching a vine, or walking to one, that vine is planned first
 * so the marker shows the vine after it. The walk between vines only changes when the set of vines or the scene
 * does, so that table is kept between ticks.
 */
@Singleton
class RouteService
{
	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;
	private final SearchCommitment commitment = new SearchCommitment();

	private WorldPoint nextVine;
	private List<WorldPoint> cachedSites = new ArrayList<>();
	private int cachedBaseX = -1;
	private int cachedBaseY = -1;
	private WalkTable cachedTable;

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

	/** Forgets the plan and the vine being searched, for when the player leaves the vines. */
	void reset()
	{
		nextVine = null;
		commitment.clear();
	}

	/**
	 * Takes in a menu click, to learn which vine the player is searching.
	 *
	 * @param action   the clicked menu action
	 * @param objectId the clicked object's id, when it was an object
	 * @param sceneX   the clicked object's scene x
	 * @param sceneY   the clicked object's scene y
	 */
	void onClick(MenuAction action, int objectId, int sceneX, int sceneY)
	{
		WorldView view = client.getTopLevelWorldView();
		boolean search = action == MenuAction.GAME_OBJECT_SECOND_OPTION && SnakeWeedIds.isVine(objectId);
		WorldPoint tile = search && view != null ? WorldPoint.fromScene(view, sceneX, sceneY, view.getPlane()) : null;
		commitment.clicked(action, objectId, tile);
	}

	/**
	 * Re-plans from where the player stands.
	 *
	 * @param tick the current game tick
	 */
	void update(int tick)
	{
		nextVine = null;
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
		WalkTable.Legs legs = table(walk, vines, sites, view).legsFrom(walk.from(mine.getSceneX(), mine.getSceneY()));
		int[] ripeAt = new int[vines.size()];
		for (int i = 0; i < ripeAt.length; i++)
			ripeAt[i] = vines.get(i).getRipeAtTick();

		int forced = forcedVine(me.getWorldLocation(), vines);
		List<Integer> plan = VineRoute.plan(legs, ripeAt, tick, config.searchMethod().expectedTicks(),
			config.lookahead(), forced);
		int next = forced != VineRoute.NONE && !plan.isEmpty() && plan.get(0) == forced ? 1 : 0;
		if (plan.size() > next)
			nextVine = vines.get(plan.get(next)).getTile();
	}

	/** The index of the vine the player is searching or walking to, or {@link VineRoute#NONE}. */
	private int forcedVine(WorldPoint player, List<Vine> vines)
	{
		WorldPoint searching = commitment.vine();
		Vine vine = searching == null ? null : tracker.get(searching);
		WorldPoint kept = commitment.update(player, vine != null && vine.isSearchable());
		for (int i = 0; kept != null && i < vines.size(); i++)
		{
			if (vines.get(i).getTile().equals(kept))
				return i;
		}

		return VineRoute.NONE;
	}

	/** The walk table for these vines, recomputed only when the vines or the scene have changed. */
	private WalkTable table(WalkDistance walk, List<Vine> vines, List<int[]> sites, WorldView view)
	{
		List<WorldPoint> tiles = new ArrayList<>();
		for (Vine vine : vines)
			tiles.add(vine.getTile());

		if (cachedTable == null || !tiles.equals(cachedSites) || view.getBaseX() != cachedBaseX
			|| view.getBaseY() != cachedBaseY)
		{
			cachedTable = new WalkTable(walk, sites);
			cachedSites = tiles;
			cachedBaseX = view.getBaseX();
			cachedBaseY = view.getBaseY();
		}

		return cachedTable;
	}
}
