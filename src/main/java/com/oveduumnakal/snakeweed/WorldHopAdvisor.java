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
 * Decides whether the vines are too contested to be worth staying on this world: other players are about and few
 * vines are ripe or about to be. Alone, a world with every vine growing back is just a wait, not a crowd. Pure and
 * static; it only advises, it never changes world.
 */
final class WorldHopAdvisor
{
	/** Ticks ahead within which a growing vine counts as about to be ripe. */
	static final int SOON_TICKS = 10;

	private WorldHopAdvisor()
	{
	}

	/**
	 * Whether to suggest hopping worlds.
	 *
	 * @param nearbyPlayers  other players near the vines
	 * @param ripeSoon       vines that are ripe now or within {@link #SOON_TICKS}
	 * @param minPlayers     suggest only when at least this many other players are near
	 * @param minRipe        suggest only when fewer than this many vines are ripe or about to be
	 * @return true when both conditions hold
	 */
	static boolean shouldHop(int nearbyPlayers, int ripeSoon, int minPlayers, int minRipe)
	{
		return nearbyPlayers >= minPlayers && ripeSoon < minRipe;
	}
}
