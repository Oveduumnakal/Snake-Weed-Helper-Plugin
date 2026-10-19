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

import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Tests {@link SearchCommitment}: which clicks start and end the vine being searched. */
public class SearchCommitmentTest
{
	private static final WorldPoint VINE = new WorldPoint(2761, 3017, 0);
	private static final WorldPoint OTHER = new WorldPoint(2758, 3016, 0);
	private static final WorldPoint BESIDE = new WorldPoint(2761, 3018, 0);
	private static final WorldPoint AWAY = new WorldPoint(2765, 3025, 0);

	private static SearchCommitment searching()
	{
		SearchCommitment commitment = new SearchCommitment();
		commitment.clicked(MenuAction.GAME_OBJECT_SECOND_OPTION, SnakeWeedIds.VINE_RIPE, VINE);
		return commitment;
	}

	@Test
	public void clickingSearchOnAVineCommitsToIt()
	{
		assertEquals(VINE, searching().vine());
	}

	@Test
	public void searchingAnotherVineReplacesIt()
	{
		SearchCommitment commitment = searching();
		commitment.clicked(MenuAction.GAME_OBJECT_SECOND_OPTION, SnakeWeedIds.VINE_RIPE, OTHER);

		assertEquals(OTHER, commitment.vine());
	}

	@Test
	public void walkingOrUsingSomethingElseEndsIt()
	{
		SearchCommitment walked = searching();
		walked.clicked(MenuAction.WALK, 0, null);
		assertNull(walked.vine());

		SearchCommitment object = searching();
		object.clicked(MenuAction.GAME_OBJECT_FIRST_OPTION, 1234, null);
		assertNull(object.vine());

		SearchCommitment npc = searching();
		npc.clicked(MenuAction.NPC_FIRST_OPTION, 1, null);
		assertNull(npc.vine());
	}

	@Test
	public void usingALogOnAKnifeKeepsIt()
	{
		SearchCommitment commitment = searching();
		commitment.clicked(MenuAction.WIDGET_TARGET_ON_WIDGET, 0, null);

		assertEquals(VINE, commitment.vine());
	}

	@Test
	public void findingTheHerbEndsIt()
	{
		SearchCommitment commitment = searching();

		assertEquals(VINE, commitment.update(BESIDE, true));
		assertNull(commitment.update(BESIDE, false));
		assertNull(commitment.vine());
	}

	@Test
	public void standingStillAwayFromTheVineGivesUpAfterAFewTicks()
	{
		SearchCommitment commitment = searching();

		for (int tick = 0; tick < SearchCommitment.STILL_TICKS - 1; tick++)
			assertEquals(VINE, commitment.update(AWAY, true));

		assertNull(commitment.update(AWAY, true));
	}

	@Test
	public void standingStillBesideTheVineKeepsIt()
	{
		SearchCommitment commitment = searching();

		for (int tick = 0; tick < 3 * SearchCommitment.STILL_TICKS; tick++)
			assertEquals(VINE, commitment.update(BESIDE, true));
	}

	@Test
	public void walkingTowardsTheVineKeepsIt()
	{
		SearchCommitment commitment = searching();

		for (int tick = 0; tick < 3 * SearchCommitment.STILL_TICKS; tick++)
			assertEquals(VINE, commitment.update(new WorldPoint(2765, 3025 - tick, 0), true));
	}
}
