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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws every vine in view: ripe vines glow in the ripe color, vines that cannot be searched are outlined in grey
 * with the time left until they grow back, so the player sees at a glance which ones to skip.
 */
class VineHighlightOverlay extends Overlay
{
	/** Length of one glow pulse in milliseconds. */
	static final int PULSE_MILLIS = 1200;

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;

	@Inject
	VineHighlightOverlay(Client client, SnakeWeedHelperConfig config, VineTracker tracker)
	{
		this.client = client;
		this.config = config;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int tick = client.getTickCount();
		double glow = config.glow() ? HighlightStyle.pulse(System.currentTimeMillis(), PULSE_MILLIS) : 1;
		graphics.setFont(FontManager.getRunescapeSmallFont());
		for (Vine vine : tracker.all())
		{
			GameObject object = tracker.object(vine.getTile());
			if (object == null)
				continue;

			if (vine.isSearchable())
			{
				HighlightStyle.draw(graphics, outline(object), config.ripeColor(), glow);
				continue;
			}

			HighlightStyle.draw(graphics, outline(object), config.greyColor(), 1);
			if (config.showCountdown())
				drawCountdown(graphics, object, label(vine, tick));
		}

		return null;
	}

	/** The countdown text for a vine that cannot be searched: seconds left, with a tilde when only estimated. */
	static String label(Vine vine, int tick)
	{
		int ticks = vine.ticksUntilRipe(tick);
		if (ticks <= 0)
			return "...";

		String seconds = (int) Math.ceil(ticks * SnakeWeedIds.SECONDS_PER_TICK) + "s";
		return vine.getState() == VineState.UNKNOWN ? "~" + seconds : seconds;
	}

	/** Writes the countdown over the vine. */
	private void drawCountdown(Graphics2D graphics, GameObject object, String label)
	{
		Point location = object.getCanvasTextLocation(graphics, label, 0);
		if (location != null)
			HighlightStyle.drawText(graphics, label, location.getX(), location.getY(), Color.WHITE, 1);
	}

	/** An object's clickbox, falling back to its convex hull when the clickbox is not available. */
	private static Shape outline(GameObject object)
	{
		Shape clickbox = object.getClickbox();
		return clickbox != null ? clickbox : object.getConvexHull();
	}
}
