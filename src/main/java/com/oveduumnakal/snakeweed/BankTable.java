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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.runelite.api.coords.WorldPoint;

/**
 * Every bank the player can path to, read from {@code banks.tsv}. The file is adapted from the Shortest Path
 * plugin's bank list (BSD 2-Clause), so the banks match the ones that plugin knows. A row lists a tile and the
 * skills, quests, varbits and varps needed to use it, so a bank behind a quest is left out until the quest is done.
 */
final class BankTable
{
	/** Resource the table is read from. */
	static final String RESOURCE = "/banks.tsv";

	private final List<BankSite> sites;

	BankTable(List<BankSite> sites)
	{
		this.sites = sites;
	}

	/**
	 * Reads the bundled bank list.
	 *
	 * @return the table, empty if the resource is missing or unreadable
	 */
	static BankTable load()
	{
		try (InputStream stream = BankTable.class.getResourceAsStream(RESOURCE))
		{
			if (stream == null)
				return new BankTable(Collections.emptyList());

			return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
		}
		catch (IOException e)
		{
			return new BankTable(Collections.emptyList());
		}
	}

	/**
	 * Parses a bank list. Blank lines, {@code #} lines and rows without a valid tile are skipped.
	 *
	 * @param reader the tab-separated text
	 * @return the table
	 * @throws IOException if the reader fails
	 */
	static BankTable parse(Reader reader) throws IOException
	{
		List<BankSite> sites = new ArrayList<>();
		BufferedReader lines = new BufferedReader(reader);
		for (String line = lines.readLine(); line != null; line = lines.readLine())
		{
			BankSite site = line.startsWith("#") || line.trim().isEmpty() ? null : parseRow(line);
			if (site != null)
				sites.add(site);
		}

		return new BankTable(sites);
	}

	/** The banks a player can use, as tiles. A tile listed on several rows counts once. */
	Set<WorldPoint> available(Requirements player)
	{
		Set<WorldPoint> tiles = new LinkedHashSet<>();
		for (BankSite site : sites)
		{
			if (site.isOpenTo(player))
				tiles.add(site.getTile());
		}

		return tiles;
	}

	/** How many rows the table holds. */
	int size()
	{
		return sites.size();
	}

	/** One row as a bank site, or {@code null} when its tile cannot be read. */
	private static BankSite parseRow(String line)
	{
		String[] columns = line.split("\t", -1);
		String[] tile = columns[0].trim().split("\\s+");
		if (tile.length != 3)
			return null;

		try
		{
			WorldPoint point = new WorldPoint(Integer.parseInt(tile[0]), Integer.parseInt(tile[1]),
				Integer.parseInt(tile[2]));
			return new BankSite(point, column(columns, 1).isEmpty() ? "" : column(columns, 1).get(0),
				column(columns, 2), column(columns, 3), column(columns, 4), column(columns, 5));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	/** The semicolon-separated entries of a column, none when it is missing or empty. */
	private static List<String> column(String[] columns, int index)
	{
		if (index >= columns.length || columns[index].trim().isEmpty())
			return Collections.emptyList();

		return Arrays.asList(columns[index].trim().split("\\s*;\\s*"));
	}
}
