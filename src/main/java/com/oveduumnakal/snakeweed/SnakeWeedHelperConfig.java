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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

/** Settings for Snake Weed Helper; sections are added as each feature lands. */
@ConfigGroup(SnakeWeedHelperConfig.GROUP)
public interface SnakeWeedHelperConfig extends Config
{
	/** Config group key, shared with the plugin so both agree on where settings are stored. */
	String GROUP = "snakeweedhelper";

	/** Key of the tick-logger toggle, watched by the plugin so the logger starts and stops live. */
	String TICK_LOGGER = "tickLogger";

	/** Tools for measuring game mechanics; not part of normal play. */
	@ConfigSection(
		name = "Developer",
		description = "Tools for measuring game mechanics.",
		position = 99,
		closedByDefault = true
	)
	String developerSection = "developer";

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
