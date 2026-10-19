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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.runelite.api.coords.WorldPoint;

/** One bank tile and what a player needs before it counts as reachable. All the requirements must be met. */
final class BankSite
{
	/** A varbit or varp requirement: an id, one of {@code =}, {@code >} or {@code <}, and a value. */
	private static final Pattern COMPARISON = Pattern.compile("(\\d{1,9})\\s*([=<>])\\s*(\\d{1,9})");

	private final WorldPoint tile;
	private final String place;
	private final List<String> skills;
	private final List<String> quests;
	private final List<String> varbits;
	private final List<String> varps;

	BankSite(WorldPoint tile, String place, List<String> skills, List<String> quests, List<String> varbits,
		List<String> varps)
	{
		this.tile = tile;
		this.place = place;
		this.skills = skills;
		this.quests = quests;
		this.varbits = varbits;
		this.varps = varps;
	}

	/** The bank's tile. */
	WorldPoint getTile()
	{
		return tile;
	}

	/** The name of the place the bank is in. */
	String getPlace()
	{
		return place;
	}

	/**
	 * Whether a player can use this bank.
	 *
	 * @param player what the player has done and reached
	 * @return true when every skill, quest, varbit and varp requirement is met
	 */
	boolean isOpenTo(Requirements player)
	{
		for (String skill : skills)
		{
			if (!skillMet(skill, player))
				return false;
		}

		for (String quest : quests)
		{
			if (!player.questFinished(quest))
				return false;
		}

		for (String varbit : varbits)
		{
			if (!comparisonMet(varbit, true, player))
				return false;
		}

		for (String varp : varps)
		{
			if (!comparisonMet(varp, false, player))
				return false;
		}

		return true;
	}

	/** Whether a requirement such as {@code 99 Cooking} is met; a malformed one is not. */
	private static boolean skillMet(String requirement, Requirements player)
	{
		String[] parts = requirement.trim().split("\\s+", 2);
		if (parts.length != 2)
			return false;

		try
		{
			return player.skillLevel(parts[1]) >= Integer.parseInt(parts[0]);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	/** Whether a requirement such as {@code 4481=1} or {@code 4130>1999} holds; a malformed one does not. */
	private static boolean comparisonMet(String requirement, boolean varbit, Requirements player)
	{
		Matcher matcher = COMPARISON.matcher(requirement.trim());
		if (!matcher.matches())
			return false;

		int id = Integer.parseInt(matcher.group(1));
		int value = Integer.parseInt(matcher.group(3));
		int actual = varbit ? player.varbit(id) : player.varp(id);
		switch (matcher.group(2))
		{
			case "=":
				return actual == value;
			case ">":
				return actual > value;
			default:
				return actual < value;
		}
	}
}
