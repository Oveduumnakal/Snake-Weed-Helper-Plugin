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

import java.lang.reflect.Proxy;

import org.junit.Test;

import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Tests {@link VineTracker}: how a vine's state follows what is seen, with a fake tick clock. */
public class VineTrackerTest
{
	private static final WorldPoint TILE = new WorldPoint(2800, 3100, 0);
	private static final WorldPoint OTHER = new WorldPoint(2805, 3102, 0);

	@Test
	public void ripeVineIsSearchableNow()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 50, false);

		Vine vine = tracker.get(TILE);
		assertTrue(vine.isSearchable());
		assertEquals(VineState.RIPE, vine.getState());
		assertEquals(0, vine.ticksUntilRipe(50));
	}

	@Test
	public void pickSeenInViewIsTimedExactly()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 10, false);
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 20, true);

		Vine vine = tracker.get(TILE);
		assertFalse(vine.isSearchable());
		assertEquals(VineState.PICKED, vine.getState());
		assertEquals(20 + SnakeWeedIds.REGROW_TICKS, vine.getRipeAtTick());
		assertEquals(SnakeWeedIds.REGROW_TICKS - 5, vine.ticksUntilRipe(25));
	}

	@Test
	public void regrowthMakesTheVineSearchableAgain()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 20, true);
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 120, true);

		assertTrue(tracker.get(TILE).isSearchable());
	}

	@Test
	public void vineFirstSeenPickedIsOnlyEstimated()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 30, false);

		Vine vine = tracker.get(TILE);
		assertEquals(VineState.UNKNOWN, vine.getState());
		assertFalse(vine.isSearchable());
		assertEquals(SnakeWeedIds.UNKNOWN_RIPE_TICKS, vine.ticksUntilRipe(30));
	}

	@Test
	public void reloadingTheSceneKeepsAnExactTimer()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 20, true);
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 60, false);

		Vine vine = tracker.get(TILE);
		assertEquals(VineState.PICKED, vine.getState());
		assertEquals(20 + SnakeWeedIds.REGROW_TICKS, vine.getRipeAtTick());
	}

	@Test
	public void reloadingTheSceneKeepsAnEstimateUntilItPasses()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 30, false);
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 40, false);

		assertEquals(30 + SnakeWeedIds.UNKNOWN_RIPE_TICKS, tracker.get(TILE).getRipeAtTick());

		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 100, false);

		assertEquals(100 + SnakeWeedIds.UNKNOWN_RIPE_TICKS, tracker.get(TILE).getRipeAtTick());
	}

	@Test
	public void ripeVineSeenPickedOnReloadBecomesAnEstimate()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 10, false);
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 80, false);

		assertEquals(VineState.UNKNOWN, tracker.get(TILE).getState());
	}

	@Test
	public void timeLeftNeverGoesBelowZero()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, TILE, 20, true);

		assertEquals(0, tracker.get(TILE).ticksUntilRipe(500));
	}

	@Test
	public void otherObjectsAreIgnored()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(12345, TILE, 10, true);

		assertNull(tracker.get(TILE));
		assertTrue(tracker.all().isEmpty());
	}

	@Test
	public void vinesAreTrackedPerTile()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 10, false);
		tracker.onSpawn(SnakeWeedIds.VINE_PICKED, OTHER, 10, true);

		assertTrue(tracker.get(TILE).isSearchable());
		assertFalse(tracker.get(OTHER).isSearchable());
		assertEquals(2, tracker.all().size());
	}

	@Test
	public void clearForgetsEveryVine()
	{
		VineTracker tracker = new VineTracker();
		tracker.onSpawn(SnakeWeedIds.VINE_RIPE, TILE, 10, false);
		tracker.clear();

		assertTrue(tracker.all().isEmpty());
		assertNull(tracker.object(TILE));
	}

	@Test
	public void forgettingObjectsKeepsTheTimersButStopsDrawing()
	{
		VineTracker tracker = new VineTracker();
		tracker.track(vineObject(SnakeWeedIds.VINE_PICKED, TILE), 20, true);
		assertNotNull(tracker.object(TILE));

		tracker.forgetObjects();

		assertNull(tracker.object(TILE));
		assertEquals(20 + SnakeWeedIds.REGROW_TICKS, tracker.get(TILE).getRipeAtTick());
	}

	/** A scene object that answers only for its id and tile, which is all the tracker asks of it. */
	private static GameObject vineObject(int id, WorldPoint tile)
	{
		return (GameObject) Proxy.newProxyInstance(GameObject.class.getClassLoader(), new Class<?>[]{GameObject.class},
			(proxy, method, args) ->
			{
				switch (method.getName())
				{
					case "getId":
						return id;
					case "getWorldLocation":
						return tile;
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					default:
						return null;
				}
			});
	}
}
