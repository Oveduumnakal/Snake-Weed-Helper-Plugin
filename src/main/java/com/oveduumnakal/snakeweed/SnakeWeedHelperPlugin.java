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

import javax.inject.Inject;

import com.google.inject.Provides;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

/** Entry point: wires the vine tracker, fail prompt, routes and overlays as the phases land. */
@PluginDescriptor(
	name = "Snake Weed Helper",
	description = "Greys out vines you cannot search, prompts a click-away after a failed search, and routes you "
		+ "between vines and the bank with Shortest Path",
	tags = {"snake", "weed", "herblore", "karamja", "tai bwo wannai", "sanfew", "overlay", "route", "3tick"}
)
public class SnakeWeedHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SnakeWeedHelperConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private VineTracker vineTracker;

	@Inject
	private VineHighlightOverlay vineOverlay;

	@Inject
	private TickClock tickClock;

	@Inject
	private RouteOverlay routeOverlay;

	@Inject
	private RouteService routeService;

	@Inject
	private TripRouter tripRouter;

	@Inject
	private HintService hintService;

	@Inject
	private HintOverlay hintOverlay;

	@Inject
	private KnownVines knownVines;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ShortestPathBridge shortestPath;

	@Inject
	private TickLogger tickLogger;

	/** True from a scene starting to load until the next tick, so the vines it brings are not read as picks. */
	private boolean sceneLoading;

	@Override
	protected void startUp()
	{
		overlayManager.add(vineOverlay);
		overlayManager.add(routeOverlay);
		overlayManager.add(hintOverlay);
		knownVines.load(config.knownVines());
		clientThread.invokeLater(this::restoreScene);
		if (config.tickLogger())
			tickLogger.start();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(vineOverlay);
		overlayManager.remove(routeOverlay);
		overlayManager.remove(hintOverlay);
		hintService.reset();
		shortestPath.clear();
		routeService.reset();
		tripRouter.reset();
		vineTracker.clear();
		tickLogger.stop();
	}

	/** Ends the scene-loading window once the tick after a load arrives, then updates the trip and its route. */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		tickClock.onTick();
		sceneLoading = false;
		if (knownVines.takeChanged())
			configManager.setConfiguration(SnakeWeedHelperConfig.GROUP, SnakeWeedHelperConfig.KNOWN_VINES,
				knownVines.save());

		if (config.showNextVine() || config.useShortestPath() || config.bankTrips())
			tripRouter.update(client.getTickCount());

		hintService.update(client.getTickCount());
	}

	/** Starts the scene-loading window, and forgets the vines when the player leaves the world. */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOADING)
			sceneLoading = true;

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			vineTracker.clear();
			tripRouter.reset();
			hintService.reset();
		}
	}

	/** Records a vine appearing: a pick or regrowth when seen in view, a baseline when loaded with a scene. */
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		vineTracker.track(object, client.getTickCount(), isLive());
		if (SnakeWeedIds.isVine(object.getId()))
			knownVines.add(object.getWorldLocation());
	}

	/** Lets go of a vine object that left the scene. */
	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		vineTracker.untrack(event.getGameObject());
	}

	/** Starts or stops the tick logger, and removes the route, as their settings are toggled. */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SnakeWeedHelperConfig.GROUP.equals(event.getGroup()))
			return;

		if (SnakeWeedHelperConfig.USE_SHORTEST_PATH.equals(event.getKey()) && !config.useShortestPath())
			shortestPath.clear();

		if (SnakeWeedHelperConfig.BANK_TRIPS.equals(event.getKey()) && !config.bankTrips())
		{
			tripRouter.reset();
			shortestPath.clear();
		}

		if (!SnakeWeedHelperConfig.TICK_LOGGER.equals(event.getKey()))
			return;

		if (config.tickLogger())
			tickLogger.start();
		else
			tickLogger.stop();
	}

	/** Whether a spawning object changed while in view, rather than arriving with a scene. */
	private boolean isLive()
	{
		return client.getGameState() == GameState.LOGGED_IN && !sceneLoading;
	}

	/** Feeds the vines already in the scene to the tracker, for when the plugin starts after login. */
	private void restoreScene()
	{
		WorldView view = client.getTopLevelWorldView();
		if (client.getGameState() != GameState.LOGGED_IN || view == null)
			return;

		Scene scene = view.getScene();
		if (scene == null)
			return;

		int tick = client.getTickCount();
		for (Tile[][] plane : scene.getTiles())
		{
			if (plane == null)
				continue;

			for (Tile[] column : plane)
				trackColumn(column, tick);
		}
	}

	/** Feeds the vines on a column of scene tiles to the tracker as a baseline. */
	private void trackColumn(Tile[] column, int tick)
	{
		if (column == null)
			return;

		for (Tile tile : column)
		{
			if (tile == null)
				continue;

			for (GameObject object : tile.getGameObjects())
			{
				if (object != null)
					trackBaseline(object, tick);
			}
		}
	}

	/** Feeds a scene object to the tracker as a baseline, and remembers it if it is a vine. */
	private void trackBaseline(GameObject object, int tick)
	{
		vineTracker.track(object, tick, false);
		if (SnakeWeedIds.isVine(object.getId()))
			knownVines.add(object.getWorldLocation());
	}

	@Provides
	SnakeWeedHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SnakeWeedHelperConfig.class);
	}
}
