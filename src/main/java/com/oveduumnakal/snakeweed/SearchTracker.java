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

/**
 * Follows one 3-tick search at a time and says when to prompt a click-away. Measured in game, counting from L, the
 * tick the fletch animation (log on knife) starts:
 * <ul>
 * <li>L + 2: the herb arrives if the search found one. No herb by then means it failed.</li>
 * <li>L + 3: a failed search that was not clicked away from starts the manual search animation, which cannot be
 * cancelled. A click-away only counts if the server handles it on this tick, so it has to be sent in the interval
 * after the L + 2 tick arrives.</li>
 * </ul>
 * L is the tick the fletch animation starts when that change is seen. The animation keeps its id from the cycle
 * before, so the change is sometimes not seen; then L is worked out from the log-on-knife click: the server handles
 * a click on the tick it was sent in only when it was sent early in that tick, and on the next tick otherwise.
 * Pure, so it is unit-testable without a client.
 */
final class SearchTracker
{
	/** Ticks after L at which the herb has arrived if there is one. */
	static final int RESOLVE_TICKS = 2;

	/** Ticks after L at which an unanswered failed search starts the manual search. */
	static final int MANUAL_TICKS = 3;

	/**
	 * Milliseconds into a tick after which a click is handled on the following tick. Measured from 15 clicks in game:
	 * all those sent up to 186 ms in were handled that tick, all from 252 ms on were handled the next. It follows the
	 * player's latency, so a slower connection moves it earlier.
	 */
	static final long CUTOFF_MILLIS = 220;

	/** Ticks the missed-window warning stays up. */
	static final int MISSED_SHOW_TICKS = 5;

	/** How far apart in ticks the vine click and the fletch start may be to count as one search. */
	private static final int PAIR_TICKS = 2;

	private static final int NONE = Integer.MIN_VALUE / 2;

	private SearchPhase phase = SearchPhase.IDLE;
	private int logClickTick = NONE;
	private int estimatedStart = NONE;
	private int fletchAnimationTick = NONE;
	private int vineClickTick = NONE;
	private int missedUntil;
	private int missed;
	private boolean clickedAway;
	private boolean lockedInManualSearch;

	/**
	 * Takes in the click of a knife on mahogany logs, which starts the delay of a 3-tick search.
	 *
	 * @param tick            the tick the click was made on
	 * @param sinceTickMillis how far into that tick the click was made
	 */
	void logClicked(int tick, long sinceTickMillis)
	{
		logClickTick = tick;
		estimatedStart = tick + (sinceTickMillis >= CUTOFF_MILLIS ? 1 : 0);
		fletchAnimationTick = NONE;
		lockedInManualSearch = false;
	}

	/**
	 * Takes in the player's animation changing to the fletch animation.
	 *
	 * @param tick the tick the change was seen on
	 */
	void fletchAnimation(int tick)
	{
		fletchAnimation(tick, false);
	}

	/**
	 * Takes in the player's animation changing to the fletch animation. A change from the manual search means the
	 * player is still locked in a search missed earlier, so the herb that comes is that search's roll and this
	 * cycle is not followed.
	 *
	 * @param tick             the tick the change was seen on
	 * @param fromManualSearch whether the animation was the manual search just before the change
	 */
	void fletchAnimation(int tick, boolean fromManualSearch)
	{
		fletchAnimationTick = tick;
		lockedInManualSearch = fromManualSearch;
	}

	/**
	 * Takes in the click that searches a vine.
	 *
	 * @param tick the tick the click was made on
	 */
	void vineSearched(int tick)
	{
		vineClickTick = tick;
	}

	/**
	 * Takes in a click that turns the player to something else, such as a walk. During the decision tick it ends the
	 * alert at once; whether it came in time is still told by the next tick.
	 */
	void clickedAway()
	{
		if (phase == SearchPhase.DECISION)
			clickedAway = true;
	}

	/**
	 * Moves on one game tick.
	 *
	 * @param tick      the current game tick
	 * @param animation the player's animation id
	 * @param herb      whether a herb arrived in the backpack this tick
	 */
	void onTick(int tick, int animation, boolean herb)
	{
		if (herb)
		{
			logClickTick = NONE;
			if (phase == SearchPhase.DECISION)
				phase = SearchPhase.IDLE;

			return;
		}

		switch (phase)
		{
			case DECISION:
				decide(tick, animation);
				break;
			case MISSED:
				if (tick >= missedUntil)
					phase = SearchPhase.IDLE;

				break;
			default:
				if (isResolved(tick))
				{
					phase = SearchPhase.DECISION;
					clickedAway = false;
					logClickTick = NONE;
				}

				break;
		}
	}

	/** The state of the search being followed. */
	SearchPhase phase()
	{
		return phase;
	}

	/** Whether the player should be told to click away right now: the search failed and they have not yet. */
	boolean alerting()
	{
		return phase == SearchPhase.DECISION && !clickedAway;
	}

	/** How many times the manual search began because the click-away was missed. */
	int missed()
	{
		return missed;
	}

	/** Forgets the search being followed and the count, for logout or when the plugin stops. */
	void reset()
	{
		phase = SearchPhase.IDLE;
		logClickTick = NONE;
		fletchAnimationTick = NONE;
		vineClickTick = NONE;
		clickedAway = false;
		lockedInManualSearch = false;
		missed = 0;
	}

	/** On the tick after the prompt: the manual search animation means the window was missed. */
	private void decide(int tick, int animation)
	{
		if (animation == SnakeWeedIds.ANIM_SEARCH)
		{
			missed++;
			phase = SearchPhase.MISSED;
			missedUntil = tick + MISSED_SHOW_TICKS;
		}
		else
		{
			phase = SearchPhase.IDLE;
		}
	}

	/** Whether this tick is when the followed search should have given its herb, and a vine was being searched. */
	private boolean isResolved(int tick)
	{
		int start = start();
		return !lockedInManualSearch && start != NONE && tick == start + RESOLVE_TICKS
			&& Math.abs(vineClickTick - start) <= PAIR_TICKS;
	}

	/** L, from the animation change when it was seen on the click's tick or the next, otherwise from the click. */
	private int start()
	{
		if (logClickTick == NONE)
			return NONE;

		boolean seen = fletchAnimationTick >= logClickTick && fletchAnimationTick <= logClickTick + 1;
		return seen ? fletchAnimationTick : estimatedStart;
	}
}
