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
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

/** Settings for Snake Weed Helper; sections are added as each feature lands. */
@ConfigGroup(SnakeWeedHelperConfig.GROUP)
public interface SnakeWeedHelperConfig extends Config
{
	/** Config group key, shared with the plugin so both agree on where settings are stored. */
	String GROUP = "snakeweedhelper";

	/** How vines you can and cannot search are drawn. */
	@ConfigSection(
		name = "Vines",
		description = "How vines you can and cannot search are drawn.",
		position = 0
	)
	String vinesSection = "vines";

	/** The prompt to click away after a failed 3-tick search. */
	@ConfigSection(
		name = "Click-away",
		description = "The prompt to click away after a failed 3-tick search.",
		position = 1
	)
	String clickAwaySection = "clickAway";

	/** Which vine to search next. */
	@ConfigSection(
		name = "Route",
		description = "Which vine to search next.",
		position = 2
	)
	String routeSection = "route";

	/** Short warnings shown while you are at the vines. */
	@ConfigSection(
		name = "Hints",
		description = "Short warnings shown while you are at the vines.",
		position = 3
	)
	String hintsSection = "hints";

	/** Alerts for when something happens while you are looking elsewhere. */
	@ConfigSection(
		name = "Notifications",
		description = "Alerts for when something happens while you are looking elsewhere.",
		position = 4
	)
	String notificationsSection = "notifications";

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
		description = "Counts down the seconds left on a vine that cannot be searched, to the tenth of a second. "
			+ "A tilde means the time is a guess because the vine was already picked when first seen.",
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
		keyName = "clickAwayPrompt",
		name = "Click-away prompt",
		description = "In the 3-tick method, on the one tick you have to click away after a search that found no "
			+ "herb, shows a large prompt and turns the vine highlights red, before the manual search animation "
			+ "takes over.",
		section = clickAwaySection,
		position = 0
	)
	default boolean clickAwayPrompt()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "clickAwayColor",
		name = "Fail color",
		description = "Color of the click-away prompt. The vine highlights and the next-vine ring turn this color "
			+ "while it shows, then go back to normal once you have clicked away.",
		section = clickAwaySection,
		position = 1
	)
	default Color clickAwayColor()
	{
		return new Color(255, 70, 40, 255);
	}

	@ConfigItem(
		keyName = "missedWarning",
		name = "Warn when missed",
		description = "Shows a short warning, with a count, when the click-away was missed and the manual search "
			+ "began.",
		section = clickAwaySection,
		position = 2
	)
	default boolean missedWarning()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNextVine",
		name = "Mark next vine",
		description = "Rings the vine the planner says to search next.",
		section = routeSection,
		position = 0
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
		position = 1
	)
	default Color targetColor()
	{
		return new Color(0, 200, 255, 230);
	}

	@ConfigItem(
		keyName = "searchMethod",
		name = "Search method",
		description = "How you search the vines. The planner uses how long that takes to judge which vines will "
			+ "have grown back by the time you reach them.",
		section = routeSection,
		position = 2
	)
	default SearchMethod searchMethod()
	{
		return SearchMethod.THREE_TICK;
	}

	@Range(min = 1, max = 4)
	@ConfigItem(
		keyName = "lookahead",
		name = "Look ahead (vines)",
		description = "How many vines after the current one the planner weighs. 1 picks the vine with the "
			+ "shortest walk from where you stand; more can pick a slightly farther vine that leaves the next "
			+ "ones close by.",
		section = routeSection,
		position = 3
	)
	default int lookahead()
	{
		return VineRoute.DEPTH;
	}

	@ConfigItem(
		keyName = "warnMissingSupplies",
		name = "Warn about missing supplies",
		description = "Warns when the knife or the mahogany logs the 3-tick method needs are not in your backpack.",
		section = hintsSection,
		position = 0
	)
	default boolean warnMissingSupplies()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bankMarker",
		name = "Time to Bank marker",
		description = "When your backpack is full at the vines, turns off the vine highlights and glows the tile "
			+ "under you with the label \"Time to Bank\". It follows you as you walk and goes away when you leave "
			+ "the vines or teleport.",
		section = hintsSection,
		position = 1
	)
	default boolean bankMarker()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "bankColor",
		name = "Time to Bank color",
		description = "Color of the tile, label and glory highlight. The default is gold.",
		section = hintsSection,
		position = 2
	)
	default Color bankColor()
	{
		return new Color(0xEB, 0xD1, 0x25, 0xFF);
	}

	@ConfigItem(
		keyName = "hopHint",
		name = "World-hop hint",
		description = "Suggests hopping worlds when other players are around and few vines are ripe. It only "
			+ "shows a message; it never changes world.",
		section = hintsSection,
		position = 3
	)
	default boolean hopHint()
	{
		return true;
	}

	@Range(min = 1, max = 20)
	@ConfigItem(
		keyName = "hopMinPlayers",
		name = "Hop hint: players",
		description = "Suggest hopping only when at least this many other players are near.",
		section = hintsSection,
		position = 4
	)
	default int hopMinPlayers()
	{
		return 2;
	}

	@Range(min = 1, max = 20)
	@ConfigItem(
		keyName = "hopMinRipeVines",
		name = "Hop hint: ripe vines",
		description = "Suggest hopping only when fewer than this many vines are ripe or about to be.",
		section = hintsSection,
		position = 5
	)
	default int hopMinRipeVines()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "notifyHarvested",
		name = "Notify when harvested",
		description = "Sends a notification each time a herb lands in your backpack, which is when a search is "
			+ "complete. Off until you turn it on.",
		section = notificationsSection,
		position = 0
	)
	default Notification notifyHarvested()
	{
		return Notification.OFF;
	}
}
