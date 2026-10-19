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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.runelite.api.coords.WorldPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests {@link BankTable}: parsing the bank list and keeping only the banks a player can use. */
public class BankTableTest
{
	private static final WorldPoint FREE = new WorldPoint(3269, 3164, 0);
	private static final WorldPoint QUEST = new WorldPoint(2852, 2954, 0);
	private static final WorldPoint GUILD = new WorldPoint(3147, 3450, 0);
	private static final WorldPoint VARBIT = new WorldPoint(2096, 3687, 0);
	private static final WorldPoint VARP = new WorldPoint(1804, 9501, 0);

	private static final String TEXT = "# comment\n"
		+ "\n"
		+ "3269 3164 0\tAl Kharid\t\t\t\t\n"
		+ "2852 2954 0\tShilo Village\t\tShilo Village\t\t\n"
		+ "3147 3450 0\tCooks' Guild\t99 Cooking\t\t\t\n"
		+ "3147 3450 0\tCooks' Guild\t\t\t4481=1\t\n"
		+ "2096 3687 0\tBuccaneers' Haven\t\t\t18369=1\t\n"
		+ "1804 9501 0\tFortis Colosseum\t\t\t\t4130>1999\n"
		+ "not a tile\tBroken\n"
		+ "1 2\tShort\n";

	/** A player whose levels, quests and varbits are set by each test. */
	private static class Player implements Requirements
	{
		private final Map<String, Integer> skills = new HashMap<>();
		private final Set<String> quests = new HashSet<>();
		private final Map<Integer, Integer> varbits = new HashMap<>();
		private final Map<Integer, Integer> varps = new HashMap<>();

		@Override
		public int skillLevel(String skill)
		{
			return skills.getOrDefault(skill, 1);
		}

		@Override
		public boolean questFinished(String quest)
		{
			return quests.contains(quest);
		}

		@Override
		public int varbit(int id)
		{
			return varbits.getOrDefault(id, 0);
		}

		@Override
		public int varp(int id)
		{
			return varps.getOrDefault(id, 0);
		}
	}

	private static BankTable table() throws IOException
	{
		return BankTable.parse(new StringReader(TEXT));
	}

	private static Set<WorldPoint> banksFor(Requirements player) throws IOException
	{
		BankTable table = table();
		return table.available(player);
	}

	@Test
	public void skipsCommentsBlankLinesAndBrokenRows() throws IOException
	{
		assertEquals(6, table().size());
	}

	@Test
	public void aPlayerWithNothingGetsOnlyTheFreeBank() throws IOException
	{
		Set<WorldPoint> banks = banksFor(new Player());

		assertEquals(1, banks.size());
		assertTrue(banks.contains(FREE));
	}

	@Test
	public void aFinishedQuestOpensItsBank() throws IOException
	{
		Player player = new Player();
		player.quests.add("Shilo Village");

		assertTrue(banksFor(player).contains(QUEST));
	}

	@Test
	public void eitherOfTwoRowsOpensTheSameTileOnce() throws IOException
	{
		Player bySkill = new Player();
		bySkill.skills.put("Cooking", 99);
		Player byVarbit = new Player();
		byVarbit.varbits.put(4481, 1);

		assertTrue(banksFor(bySkill).contains(GUILD));
		assertTrue(banksFor(byVarbit).contains(GUILD));
		assertFalse(banksFor(new Player()).contains(GUILD));
	}

	@Test
	public void varbitAndVarpComparisonsAreChecked() throws IOException
	{
		Player player = new Player();
		player.varbits.put(18369, 1);
		player.varps.put(4130, 2000);

		Set<WorldPoint> banks = banksFor(player);
		assertTrue(banks.contains(VARBIT));
		assertTrue(banks.contains(VARP));

		player.varps.put(4130, 1999);

		assertFalse(banksFor(player).contains(VARP));
	}

	@Test
	public void theBundledListLoadsAndHonoursRequirements()
	{
		BankTable bundled = BankTable.load();

		assertTrue(bundled.size() > 250);
		assertTrue(bundled.available(new Player()).contains(FREE));
		assertFalse(bundled.available(new Player()).contains(QUEST));
	}
}
