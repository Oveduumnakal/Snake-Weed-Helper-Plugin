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
 * Pure state machine for the trip to the bank and back. A full backpack carrying snake weed sends the player to the
 * bank; opening the bank starts banking; closing it with no snake weed left sends the player back to the vines;
 * arriving at the vines starts harvesting again. Every state also has a way out if the player does something else,
 * such as making room without banking, so it never gets stuck.
 */
final class TripTracker
{
	private TripState state = TripState.HARVESTING;

	/** The current state. */
	TripState getState()
	{
		return state;
	}

	/** Goes back to harvesting, for when trips are switched off or the player logs out. */
	void reset()
	{
		state = TripState.HARVESTING;
	}

	/**
	 * Moves to the next state from what the player is doing now.
	 *
	 * @param freeSlots   empty backpack slots
	 * @param carriesWeed whether any snake weed, grimy or clean, is in the backpack
	 * @param bankOpen    whether the bank interface is open
	 * @param nearVines   whether the player is at the vines
	 * @param bankAtSlots the trip starts when free slots are at or below this
	 * @return the new state
	 */
	TripState update(int freeSlots, boolean carriesWeed, boolean bankOpen, boolean nearVines, int bankAtSlots)
	{
		boolean full = carriesWeed && freeSlots <= bankAtSlots;
		switch (state)
		{
			case HARVESTING:
				if (full)
					state = TripState.TO_BANK;

				break;
			case TO_BANK:
				if (bankOpen)
					state = TripState.BANKING;
				else if (!full)
					state = TripState.HARVESTING;

				break;
			case BANKING:
				if (bankOpen)
					break;

				if (!carriesWeed)
					state = TripState.TO_VINES;
				else if (full)
					state = TripState.TO_BANK;
				else
					state = TripState.HARVESTING;

				break;
			default:
				if (bankOpen)
					state = TripState.BANKING;
				else if (nearVines)
					state = TripState.HARVESTING;

				break;
		}

		return state;
	}
}
