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

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

/** Settings for Snake Weed Helper; sections are added as each feature lands. */
@ConfigGroup(SnakeWeedHelperConfig.GROUP)
public interface SnakeWeedHelperConfig extends Config
{
	/** Config group key, shared with the plugin so both agree on where settings are stored. */
	String GROUP = "snakeweedhelper";

	/** Key of the Shortest Path toggle, watched by the plugin so a route is removed when it is switched off. */
	String USE_SHORTEST_PATH = "useShortestPath";

	/** Key of the tick-logger toggle, watched by the plugin so the logger starts and stops live. */
	String TICK_LOGGER = "tickLogger";

	/** How vines you can and cannot search are drawn. */
	@ConfigSection(
		name = "Vines",
		description = "How vines you can and cannot search are drawn.",
		position = 0
	)
	String vinesSection = "vines";

	/** Which vine to go to next, and how the route to it is shown. */
	@ConfigSection(
		name = "Route",
		description = "Which vine to go to next, and how the route to it is shown.",
		position = 1
	)
	String routeSection = "route";

	/** Tools for measuring game mechanics; not part of normal play. */
	@ConfigSection(
		name = "Developer",
		description = "Tools for measuring game mechanics.",
		position = 99,
		closedByDefault = true
	)
	String developerSection = "developer";

	@Alpha
	@ConfigItem(
		keyName = "ripeColor",
		name = "Ripe vine",
		description = "Color of a vine you can search.",
		section = vinesSection,
		position = 0
	)
	default Color ripeColor()
	{
		return new Color(46, 219, 90, 230);
	}

	@Alpha
	@ConfigItem(
		keyName = "greyColor",
		name = "Vine to skip",
		description = "Color of a vine that cannot be searched yet.",
		section = vinesSection,
		position = 1
	)
	default Color greyColor()
	{
		return new Color(140, 140, 140, 190);
	}

	@ConfigItem(
		keyName = "showCountdown",
		name = "Show regrow countdown",
		description = "Shows the seconds left on a vine that cannot be searched. A tilde means the time is a "
			+ "guess because the vine was already picked when first seen.",
		section = vinesSection,
		position = 2
	)
	default boolean showCountdown()
	{
		return true;
	}

	@ConfigItem(
		keyName = "glow",
		name = "Glow ripe vines",
		description = "Pulses the highlight on vines you can search. Off keeps it steady.",
		section = vinesSection,
		position = 3
	)
	default boolean glow()
	{
		return true;
	}

	@ConfigItem(
		keyName = USE_SHORTEST_PATH,
		name = "Use 'Shortest Path' plugin",
		description = "If you have the 'Shortest Path' plugin downloaded, it will be used to show routes to the "
			+ "next vine and the bank",
		section = routeSection,
		position = 0
	)
	default boolean useShortestPath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNextVine",
		name = "Mark next vine",
		description = "Rings the vine the planner says to search next.",
		section = routeSection,
		position = 1
	)
	default boolean showNextVine()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "targetColor",
		name = "Next vine",
		description = "Color of the ring on the vine to search next.",
		section = routeSection,
		position = 2
	)
	default Color targetColor()
	{
		return new Color(0, 200, 255, 230);
	}

	@Range(min = 1, max = 20)
	@ConfigItem(
		keyName = "searchTicks",
		name = "Ticks per search",
		description = "How many game ticks one search takes you, which the planner uses to weigh walking "
			+ "against waiting for a vine to grow back.",
		section = routeSection,
		position = 3
	)
	default int searchTicks()
	{
		return 3;
	}

	@ConfigItem(
		keyName = TICK_LOGGER,
		name = "Tick logger",
		description = "Writes every tick, animation, chat line, click and vine change to "
			+ "~/.runelite/snake-weed-helper/ticklog-*.tsv. For measuring search timings; leave off in play.",
		section = developerSection,
		position = 0
	)
	default boolean tickLogger()
	{
		return false;
	}
}
