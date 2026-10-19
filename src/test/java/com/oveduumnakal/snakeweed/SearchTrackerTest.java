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

/**
 * Tests {@link SearchTracker} by replaying searches from the first 3-tick log: tick numbers are the ones in
 * {@code ticklog-20261018-215536.tsv}.
 */
public class SearchTrackerTest
{
	private static final int FLETCH = SnakeWeedIds.ANIM_FLETCH;
	private static final int SEARCH = SnakeWeedIds.ANIM_SEARCH;
	private static final int IDLE_ANIM = -1;

	/** Runs the tracker from one tick to another with the animation held, and returns the phase on the last. */
	private static SearchPhase run(SearchTracker tracker, int from, int to, int animation)
	{
		for (int tick = from; tick <= to; tick++)
			tracker.onTick(tick, animation, false);

		return tracker.phase();
	}

	@Test
	public void aSuccessfulSearchNeverPrompts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(369, 489);
		tracker.fletchAnimation(370);
		tracker.vineSearched(371);

		assertEquals(SearchPhase.IDLE, run(tracker, 370, 371, FLETCH));
		tracker.onTick(372, FLETCH, true);
		assertEquals(SearchPhase.IDLE, tracker.phase());
		assertEquals(SearchPhase.IDLE, run(tracker, 373, 380, FLETCH));
	}

	@Test
	public void aFailedSearchPromptsTwoTicksAfterTheFletchStarts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(159, 64);
		tracker.fletchAnimation(159);
		tracker.vineSearched(159);

		assertEquals(SearchPhase.IDLE, run(tracker, 159, 160, FLETCH));
		tracker.onTick(161, FLETCH, false);
		assertEquals(SearchPhase.DECISION, tracker.phase());
	}

	@Test
	public void aCycleStartedWhileStillInAMissedManualSearchNeverPrompts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(159, 64);
		tracker.fletchAnimation(159, true);
		tracker.vineSearched(159);

		assertEquals(SearchPhase.IDLE, run(tracker, 159, 161, FLETCH));
	}

	@Test
	public void clickingAwayInTimeClearsThePromptWithoutAMiss()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(159, 64);
		tracker.fletchAnimation(159);
		tracker.vineSearched(159);
		run(tracker, 159, 161, FLETCH);

		tracker.onTick(162, FLETCH, false);

		assertEquals(SearchPhase.IDLE, tracker.phase());
		assertEquals(0, tracker.missed());
	}

	@Test
	public void clickingAwayEndsTheAlertAtOnce()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(159, 64);
		tracker.fletchAnimation(159);
		tracker.vineSearched(159);
		run(tracker, 159, 160, FLETCH);
		tracker.onTick(161, FLETCH, false);
		assertEquals(true, tracker.alerting());

		tracker.clickedAway();

		assertEquals(false, tracker.alerting());
		tracker.onTick(162, FLETCH, false);
		assertEquals(SearchPhase.IDLE, tracker.phase());
		assertEquals(0, tracker.missed());
	}

	@Test
	public void aClickThatCameTooLateStillCountsAsMissedWhenTheManualSearchStarts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(640, 485);
		tracker.fletchAnimation(640);
		tracker.vineSearched(640);
		run(tracker, 640, 641, FLETCH);
		tracker.onTick(642, FLETCH, false);

		tracker.clickedAway();
		tracker.onTick(643, SEARCH, false);

		assertEquals(SearchPhase.MISSED, tracker.phase());
		assertEquals(1, tracker.missed());
	}

	@Test
	public void aClickOutsideTheDecisionTickDoesNothing()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.clickedAway();

		assertEquals(false, tracker.alerting());
	}

	@Test
	public void theNextSearchIsStillFollowedWhenItsLogClickCameDuringTheDecision()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(159, 64);
		tracker.fletchAnimation(159);
		tracker.vineSearched(159);
		run(tracker, 159, 160, FLETCH);
		tracker.onTick(161, FLETCH, false);

		tracker.logClicked(162, 50);
		tracker.fletchAnimation(162);
		tracker.vineSearched(162);
		tracker.onTick(162, FLETCH, false);
		run(tracker, 163, 163, FLETCH);
		tracker.onTick(164, FLETCH, false);

		assertEquals(SearchPhase.DECISION, tracker.phase());
	}

	@Test
	public void theManualSearchStartingIsAMissedWindow()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(196, 387);
		tracker.fletchAnimation(197);
		tracker.vineSearched(197);
		run(tracker, 197, 198, FLETCH);
		tracker.onTick(199, FLETCH, false);
		assertEquals(SearchPhase.DECISION, tracker.phase());

		tracker.onTick(200, SEARCH, false);

		assertEquals(SearchPhase.MISSED, tracker.phase());
		assertEquals(1, tracker.missed());
	}

	@Test
	public void theMissedWarningFadesAfterAFewTicks()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(640, 485);
		tracker.fletchAnimation(640);
		tracker.vineSearched(640);
		run(tracker, 640, 641, FLETCH);
		tracker.onTick(642, FLETCH, false);
		tracker.onTick(643, SEARCH, false);

		assertEquals(SearchPhase.MISSED, run(tracker, 644, 643 + SearchTracker.MISSED_SHOW_TICKS - 1, SEARCH));
		tracker.onTick(643 + SearchTracker.MISSED_SHOW_TICKS, SEARCH, false);
		assertEquals(SearchPhase.IDLE, tracker.phase());
		assertEquals(1, tracker.missed());
	}

	@Test
	public void withoutTheAnimationChangeAnEarlyClickStartsOnItsOwnTick()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(384, 186);
		tracker.vineSearched(385);

		run(tracker, 384, 385, FLETCH);
		tracker.onTick(386, FLETCH, false);

		assertEquals(SearchPhase.DECISION, tracker.phase());
	}

	@Test
	public void withoutTheAnimationChangeALateClickStartsOnTheNextTick()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(426, 600);
		tracker.vineSearched(427);

		run(tracker, 426, 428, FLETCH);
		assertEquals(SearchPhase.IDLE, tracker.phase());
		tracker.onTick(429, FLETCH, true);
		assertEquals(SearchPhase.IDLE, tracker.phase());
	}

	@Test
	public void theClickCutoffSitsBetweenTheMeasuredEarlyAndLateClicks()
	{
		assertEquals(true, SearchTracker.CUTOFF_MILLIS > 186);
		assertEquals(true, SearchTracker.CUTOFF_MILLIS < 252);
	}

	@Test
	public void aLogClickWithoutASearchedVineNeverPrompts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(100, 100);
		tracker.fletchAnimation(100);

		assertEquals(SearchPhase.IDLE, run(tracker, 100, 110, FLETCH));
	}

	@Test
	public void aStandardSearchWithoutTheFletchNeverPrompts()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.vineSearched(87);

		assertEquals(SearchPhase.IDLE, run(tracker, 87, 110, SEARCH));
	}

	@Test
	public void resettingForgetsTheSearchAndTheCount()
	{
		SearchTracker tracker = new SearchTracker();
		tracker.logClicked(196, 387);
		tracker.fletchAnimation(197);
		tracker.vineSearched(197);
		run(tracker, 197, 198, FLETCH);
		tracker.onTick(199, FLETCH, false);
		tracker.onTick(200, SEARCH, false);

		tracker.reset();

		assertEquals(SearchPhase.IDLE, tracker.phase());
		assertEquals(0, tracker.missed());
		assertEquals(SearchPhase.IDLE, run(tracker, 201, 210, IDLE_ANIM));
	}
}
