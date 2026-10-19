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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/**
 * Phase 1 measurement tool, removed before release: writes one tab-separated line per game event to
 * {@code ~/.runelite/snake-weed-helper/ticklog-<time>.tsv} so the real search timings can be read off afterwards.
 * Columns are the game tick, the wall-clock milliseconds, a kind, and details. Kinds: {@code TICK} (every tick),
 * {@code ANIM} (local player animation change), {@code CHAT}, {@code INV} (inventory change), {@code CLICK} (menu
 * option), {@code STAT} (experience change), {@code VINE+} / {@code VINE-} (vine object spawn and despawn) and
 * {@code VINE_SCENE} (every vine in the loaded scene, dumped on login and on start). Vines are found by name, so no
 * object id is assumed. Only game, spam and engine chat is logged, never other players' messages.
 */
@Singleton
class TickLogger
{
	private static final Logger log = LoggerFactory.getLogger(TickLogger.class);

	/** Lower-case text an object's name must contain to be logged as a vine. */
	private static final String VINE_NAME = "vine";

	/** Players within this many tiles count as nearby in the {@code TICK} line. */
	private static final int NEARBY_RADIUS = 15;

	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;

	private BufferedWriter out;
	private int lastAnimation = -1;

	@Inject
	TickLogger(Client client, ClientThread clientThread, EventBus eventBus)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
	}

	/** Opens a new log file, starts listening to game events, and dumps the vines already in the scene. */
	void start()
	{
		if (out != null)
			return;

		File dir = new File(RuneLite.RUNELITE_DIR, "snake-weed-helper");
		Path file = new File(dir, "ticklog-" + LocalDateTime.now().format(STAMP) + ".tsv").toPath();
		try
		{
			Files.createDirectories(dir.toPath());
			out = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			log.warn("Tick logger could not open {}", file, e);
			return;
		}

		log.info("Tick logger writing to {}", file);
		eventBus.register(this);
		write("START", "tickms=600");
		clientThread.invokeLater(this::dumpVines);
	}

	/** Stops listening and closes the log file. */
	void stop()
	{
		if (out == null)
			return;

		eventBus.unregister(this);
		try
		{
			out.close();
		}
		catch (IOException e)
		{
			log.warn("Tick logger could not close its file", e);
		}

		out = null;
		lastAnimation = -1;
	}

	/** Logs the player's state every tick: position, animations, target, backpack and nearby players. */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player me = client.getLocalPlayer();
		if (me == null)
			return;

		Actor target = me.getInteracting();
		int[] counts = inventoryCounts();
		write("TICK", "pos=" + me.getWorldLocation().getX() + "," + me.getWorldLocation().getY()
			+ "," + me.getWorldLocation().getPlane()
			+ " anim=" + me.getAnimation()
			+ " pose=" + me.getPoseAnimation()
			+ " target=" + (target == null ? "-" : target.getName())
			+ " grimy=" + counts[0]
			+ " free=" + counts[4]
			+ " nearby=" + nearbyPlayers(me));
	}

	/** Logs each change of the local player's animation. */
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (actor != client.getLocalPlayer())
			return;

		write("ANIM", "from=" + lastAnimation + " to=" + actor.getAnimation());
		lastAnimation = actor.getAnimation();
	}

	/** Logs game, spam and engine messages, which is where a search result would appear. */
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM && type != ChatMessageType.ENGINE)
			return;

		write("CHAT", type + " " + event.getMessage());
	}

	/** Logs backpack contents whenever the backpack changes. */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
			return;

		int[] counts = inventoryCounts();
		write("INV", "grimy=" + counts[0] + " clean=" + counts[1] + " knife=" + counts[2]
			+ " logs=" + counts[3] + " free=" + counts[4]);
	}

	/** Logs every menu click, so the click that starts or cancels a search is visible. */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		write("CLICK", "option=" + event.getMenuOption() + " target=" + event.getMenuTarget()
			+ " action=" + event.getMenuAction() + " id=" + event.getId()
			+ " p0=" + event.getParam0() + " p1=" + event.getParam1() + " item=" + event.getItemId());
	}

	/** Logs experience changes, which mark the tick a fletch or a find happened on. */
	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		write("STAT", event.getSkill() + " xp=" + event.getXp() + " level=" + event.getBoostedLevel());
	}

	/** Logs a vine appearing, which is how a picked or regrown vine shows up. */
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		if (isVine(object.getId()))
			write("VINE+", describe(object));
	}

	/** Logs a vine disappearing. */
	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject object = event.getGameObject();
		if (isVine(object.getId()))
			write("VINE-", describe(object));
	}

	/** Dumps the vines in a freshly loaded scene. */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
			clientThread.invokeLater(this::dumpVines);
	}

	/** Writes every vine object in the loaded scene, then how many there were. */
	private void dumpVines()
	{
		WorldView view = client.getTopLevelWorldView();
		if (client.getGameState() != GameState.LOGGED_IN || view == null)
			return;

		Scene scene = view.getScene();
		if (scene == null)
			return;

		int found = 0;
		for (Tile[][] plane : scene.getTiles())
		{
			if (plane == null)
				continue;

			for (Tile[] column : plane)
				found += dumpColumn(column);
		}

		write("VINE_SCENE_COUNT", String.valueOf(found));
	}

	/** Writes the vines on one column of scene tiles and returns how many were written. */
	private int dumpColumn(Tile[] column)
	{
		int found = 0;
		if (column == null)
			return found;

		for (Tile tile : column)
		{
			if (tile == null)
				continue;

			for (GameObject object : tile.getGameObjects())
			{
				if (object != null && isVine(object.getId())
					&& tile.getSceneLocation().equals(object.getSceneMinLocation()))
				{
					write("VINE_SCENE", describe(object));
					found++;
				}
			}
		}

		return found;
	}

	/** Whether an object id is named like a vine, looking through a multi-object's current form. */
	private boolean isVine(int id)
	{
		ObjectComposition composition = client.getObjectDefinition(id);
		if (composition == null)
			return false;

		String name = composition.getName();
		if ((name == null || "null".equals(name)) && composition.getImpostorIds() != null)
		{
			ObjectComposition impostor = composition.getImpostor();
			name = impostor == null ? null : impostor.getName();
		}

		return name != null && name.toLowerCase(Locale.ROOT).contains(VINE_NAME);
	}

	/** An object as one log field: id, name, tile. */
	private String describe(GameObject object)
	{
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		return "id=" + object.getId() + " name=" + (composition == null ? "?" : composition.getName())
			+ " tile=" + object.getWorldLocation().getX() + "," + object.getWorldLocation().getY()
			+ "," + object.getWorldLocation().getPlane();
	}

	/** Counts in the backpack: grimy snake weed, clean snake weed, knives, mahogany logs, free slots. */
	private int[] inventoryCounts()
	{
		int[] counts = new int[5];
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return counts;

		int used = 0;
		for (Item item : inventory.getItems())
		{
			if (item.getId() == -1)
				continue;

			used++;
			if (item.getId() == ItemID.UNIDENTIFIED_SNAKE_WEED)
				counts[0] += item.getQuantity();
			else if (item.getId() == ItemID.SNAKE_WEED)
				counts[1] += item.getQuantity();
			else if (item.getId() == ItemID.KNIFE)
				counts[2] += item.getQuantity();
			else if (item.getId() == ItemID.MAHOGANY_LOGS)
				counts[3] += item.getQuantity();
		}

		counts[4] = 28 - used;
		return counts;
	}

	/** How many other players stand within {@link #NEARBY_RADIUS} tiles of the local player. */
	private int nearbyPlayers(Player me)
	{
		WorldView view = client.getTopLevelWorldView();
		int nearby = 0;
		for (Player player : view.players())
		{
			if (player != me && player.getWorldLocation().distanceTo(me.getWorldLocation()) <= NEARBY_RADIUS)
				nearby++;
		}

		return nearby;
	}

	/**
	 * Adds a line from another part of the plugin, such as a prompt being shown, so a log shows what the plugin
	 * decided as well as what the game did. Does nothing while the logger is off.
	 *
	 * @param kind    the kind of line
	 * @param details what happened
	 */
	void note(String kind, String details)
	{
		write(kind, details);
	}

	/** Appends one line: tick, wall-clock milliseconds, kind, details. Closes the logger if the write fails. */
	private void write(String kind, String details)
	{
		if (out == null)
			return;

		try
		{
			out.write(client.getTickCount() + "\t" + System.currentTimeMillis() + "\t" + kind + "\t" + details + "\n");
			out.flush();
		}
		catch (IOException e)
		{
			log.warn("Tick logger write failed, stopping", e);
			stop();
		}
	}
}
