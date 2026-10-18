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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;

/**
 * Hands routes to the Shortest Path plugin through {@link PluginMessage}s, the way Quest Helper does, so this plugin
 * needs no dependency on it. A route is sent only when the target set changes, and a clear only when a route this
 * plugin sent is showing, so it never wipes a route that someone else set. Shortest Path ignores the messages when
 * it is not installed.
 */
@Singleton
class ShortestPathBridge
{
	/** The namespace Shortest Path listens to. */
	static final String NAMESPACE = "shortestpath";

	/** Message name that draws a route. */
	static final String PATH = "path";

	/** Message name that removes the route. */
	static final String CLEAR = "clear";

	private final EventBus eventBus;
	private final SnakeWeedHelperConfig config;

	private Set<WorldPoint> sent;

	@Inject
	ShortestPathBridge(EventBus eventBus, SnakeWeedHelperConfig config)
	{
		this.eventBus = eventBus;
		this.config = config;
	}

	/**
	 * Draws a route to the nearest of some targets, unless this exact set is already showing.
	 *
	 * @param start   where the route begins, normally the player's tile
	 * @param targets the tiles to reach; Shortest Path takes the nearest by real path
	 */
	void show(WorldPoint start, Set<WorldPoint> targets)
	{
		if (!config.useShortestPath() || targets.isEmpty() || targets.equals(sent))
			return;

		eventBus.post(new PluginMessage(NAMESPACE, PATH, message(start, targets)));
		sent = new HashSet<>(targets);
	}

	/** Removes the route, if this plugin drew one. */
	void clear()
	{
		if (sent == null)
			return;

		eventBus.post(new PluginMessage(NAMESPACE, CLEAR));
		sent = null;
	}

	/** Forgets what was sent without removing the route, for when Shortest Path is known to have dropped it. */
	void forget()
	{
		sent = null;
	}

	/**
	 * Builds the data of a {@code path} message. One target is sent as a bare point, which every Shortest Path
	 * version takes; several are sent as a set, which newer versions take.
	 *
	 * @param start   where the route begins
	 * @param targets the tiles to reach
	 * @return the message data
	 */
	static Map<String, Object> message(WorldPoint start, Set<WorldPoint> targets)
	{
		Map<String, Object> data = new HashMap<>();
		data.put("start", start);
		data.put("target", targets.size() == 1 ? targets.iterator().next() : new HashSet<>(targets));
		return data;
	}
}
