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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests {@link TripTracker}: the harvest, bank, and return loop, and the ways out of each state. */
public class TripTrackerTest
{
	private static TripState step(TripTracker trip, int free, boolean weed, boolean bank, boolean near)
	{
		return trip.update(free, weed, bank, near, 0);
	}

	@Test
	public void startsHarvesting()
	{
		assertEquals(TripState.HARVESTING, new TripTracker().getState());
	}

	@Test
	public void roomLeftKeepsHarvesting()
	{
		TripTracker trip = new TripTracker();

		assertEquals(TripState.HARVESTING, step(trip, 5, true, false, true));
	}

	@Test
	public void aFullBackpackOfWeedStartsTheTripToTheBank()
	{
		TripTracker trip = new TripTracker();

		assertEquals(TripState.TO_BANK, step(trip, 0, true, false, true));
	}

	@Test
	public void aFullBackpackWithoutWeedDoesNotStartATrip()
	{
		TripTracker trip = new TripTracker();

		assertEquals(TripState.HARVESTING, step(trip, 0, false, false, true));
	}

	@Test
	public void theWholeLoop()
	{
		TripTracker trip = new TripTracker();

		assertEquals(TripState.TO_BANK, step(trip, 0, true, false, true));
		assertEquals(TripState.TO_BANK, step(trip, 0, true, false, false));
		assertEquals(TripState.BANKING, step(trip, 0, true, true, false));
		assertEquals(TripState.BANKING, step(trip, 27, false, true, false));
		assertEquals(TripState.TO_VINES, step(trip, 27, false, false, false));
		assertEquals(TripState.TO_VINES, step(trip, 27, false, false, false));
		assertEquals(TripState.HARVESTING, step(trip, 27, false, false, true));
	}

	@Test
	public void makingRoomOnTheWayCancelsTheTrip()
	{
		TripTracker trip = new TripTracker();
		step(trip, 0, true, false, true);

		assertEquals(TripState.HARVESTING, step(trip, 4, true, false, true));
	}

	@Test
	public void closingTheBankStillFullSendsYouBack()
	{
		TripTracker trip = new TripTracker();
		step(trip, 0, true, false, true);
		step(trip, 0, true, true, false);

		assertEquals(TripState.TO_BANK, step(trip, 0, true, false, false));
	}

	@Test
	public void closingTheBankWithRoomAndSomeWeedResumesHarvesting()
	{
		TripTracker trip = new TripTracker();
		step(trip, 0, true, false, true);
		step(trip, 0, true, true, false);

		assertEquals(TripState.HARVESTING, step(trip, 10, true, false, false));
	}

	@Test
	public void openingTheBankOnTheWayBackBanksAgain()
	{
		TripTracker trip = new TripTracker();
		step(trip, 0, true, false, true);
		step(trip, 0, true, true, false);
		step(trip, 27, false, false, false);

		assertEquals(TripState.BANKING, step(trip, 27, false, true, false));
	}

	@Test
	public void theThresholdMovesWhereTheTripStarts()
	{
		TripTracker trip = new TripTracker();

		assertEquals(TripState.HARVESTING, trip.update(3, true, false, true, 2));
		assertEquals(TripState.TO_BANK, trip.update(2, true, false, true, 2));
	}

	@Test
	public void resetGoesBackToHarvesting()
	{
		TripTracker trip = new TripTracker();
		step(trip, 0, true, false, true);
		trip.reset();

		assertEquals(TripState.HARVESTING, trip.getState());
	}
}
