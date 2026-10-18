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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests {@link ShortestPathBridge} with a real event bus: what is sent, and when it is not. */
public class ShortestPathBridgeTest
{
	private static final WorldPoint START = new WorldPoint(2800, 3100, 0);
	private static final WorldPoint A = new WorldPoint(2805, 3102, 0);
	private static final WorldPoint B = new WorldPoint(2810, 3104, 0);

	private final List<PluginMessage> messages = new ArrayList<>();
	private EventBus bus;

	/** Collects every plugin message posted to the bus. */
	private class Collector
	{
		@Subscribe
		public void onPluginMessage(PluginMessage message)
		{
			messages.add(message);
		}
	}

	@Before
	public void setUp()
	{
		bus = new EventBus();
		bus.register(new Collector());
	}

	private Object field(int index, String key)
	{
		PluginMessage message = messages.get(index);
		Map<String, Object> data = message.getData();
		return data.get(key);
	}

	private ShortestPathBridge bridge(boolean enabled)
	{
		return new ShortestPathBridge(bus, new SnakeWeedHelperConfig()
		{
			@Override
			public boolean useShortestPath()
			{
				return enabled;
			}
		});
	}

	@Test
	public void sendsOnePathAndNotARepeat()
	{
		ShortestPathBridge bridge = bridge(true);
		bridge.show(START, Collections.singleton(A));
		bridge.show(START, Collections.singleton(A));

		assertEquals(1, messages.size());
		assertEquals(ShortestPathBridge.NAMESPACE, messages.get(0).getNamespace());
		assertEquals(ShortestPathBridge.PATH, messages.get(0).getName());
		assertEquals(START, field(0, "start"));
		assertEquals(A, field(0, "target"));
	}

	@Test
	public void sendsAgainWhenTheTargetChanges()
	{
		ShortestPathBridge bridge = bridge(true);
		bridge.show(START, Collections.singleton(A));
		bridge.show(START, Collections.singleton(B));

		assertEquals(2, messages.size());
		assertEquals(B, field(1, "target"));
	}

	@Test
	public void severalTargetsGoAsASet()
	{
		bridge(true).show(START, new HashSet<>(Arrays.asList(A, B)));

		Object target = field(0, "target");
		assertTrue(target instanceof Set);
		assertEquals(new HashSet<>(Arrays.asList(A, B)), target);
	}

	@Test
	public void clearOnlyFollowsAnEarlierSend()
	{
		ShortestPathBridge bridge = bridge(true);
		bridge.clear();

		assertTrue(messages.isEmpty());

		bridge.show(START, Collections.singleton(A));
		bridge.clear();
		bridge.clear();

		assertEquals(2, messages.size());
		assertEquals(ShortestPathBridge.CLEAR, messages.get(1).getName());
	}

	@Test
	public void sameTargetShowsAgainAfterAClear()
	{
		ShortestPathBridge bridge = bridge(true);
		bridge.show(START, Collections.singleton(A));
		bridge.clear();
		bridge.show(START, Collections.singleton(A));

		assertEquals(3, messages.size());
		assertEquals(ShortestPathBridge.PATH, messages.get(2).getName());
	}

	@Test
	public void sendsNothingWhenSwitchedOffOrWithoutTargets()
	{
		bridge(false).show(START, Collections.singleton(A));
		bridge(true).show(START, Collections.<WorldPoint>emptySet());

		assertTrue(messages.isEmpty());
	}

	@Test
	public void forgettingLetsTheSameTargetBeSentAgain()
	{
		ShortestPathBridge bridge = bridge(true);
		bridge.show(START, Collections.singleton(A));
		bridge.forget();
		bridge.show(START, Collections.singleton(A));

		assertEquals(2, messages.size());
	}
}
