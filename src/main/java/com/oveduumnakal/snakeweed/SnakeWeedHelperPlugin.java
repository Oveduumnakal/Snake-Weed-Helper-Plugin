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
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

/** Entry point: wires the vine tracker, next-vine planner, hints and overlays. */
@PluginDescriptor(
	name = "Snake Weed Helper",
	description = "Greys out vines you cannot search, prompts a click-away after a failed search, and marks the "
		+ "best vine to search next",
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
	private HintService hintService;

	@Inject
	private HintOverlay hintOverlay;

	@Inject
	private HarvestNotifier harvestNotifier;

	@Inject
	private SearchService searchService;

	@Inject
	private ClickAwayOverlay clickAwayOverlay;

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
		overlayManager.add(clickAwayOverlay);
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
		overlayManager.remove(clickAwayOverlay);
		hintService.reset();
		harvestNotifier.reset();
		searchService.reset();
		routeService.reset();
		vineTracker.clear();
		tickLogger.stop();
	}

	/** Ends the scene-loading window once the tick after a load arrives, then re-plans the next vine. */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		tickClock.onTick();
		sceneLoading = false;
		if (config.showNextVine())
			routeService.update(client.getTickCount());
		else
			routeService.reset();

		hintService.update(client.getTickCount());
		searchService.onTick();
	}

	/** Tells the search tracker when the player's fletch animation (re)starts, which times a 3-tick search. */
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() == client.getLocalPlayer() && event.getActor().getAnimation() == SnakeWeedIds.ANIM_FLETCH)
			searchService.onFletchAnimation();
	}

	/** Sends the harvest notification when a herb lands in the backpack. */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		harvestNotifier.onItemsChanged(event.getContainerId(), event.getItemContainer());
	}

	/** Tells the planner which vine the player clicked to search, or that they turned to something else. */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		routeService.onClick(event.getMenuAction(), event.getId(), event.getParam0(), event.getParam1());
		searchService.onClick(event.getMenuAction(), event.getItemId(), event.getId());
	}

	/** Starts the scene-loading window, and forgets the vines when the player leaves the world. */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOADING)
		{
			sceneLoading = true;
			vineTracker.forgetObjects();
		}

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			vineTracker.clear();
			routeService.reset();
			hintService.reset();
			harvestNotifier.reset();
			searchService.reset();
		}
	}

	/** Records a vine appearing: a pick or regrowth when seen in view, a baseline when loaded with a scene. */
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		vineTracker.track(object, client.getTickCount(), isLive());
	}

	/** Lets go of a vine object that left the scene. */
	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		vineTracker.untrack(event.getGameObject());
	}

	/** Starts or stops the tick logger as its setting is toggled. */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SnakeWeedHelperConfig.GROUP.equals(event.getGroup()))
			return;

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
					vineTracker.track(object, tick, false);
			}
		}
	}

	@Provides
	SnakeWeedHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SnakeWeedHelperConfig.class);
	}
}
