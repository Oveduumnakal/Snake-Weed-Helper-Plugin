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
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Rings the vine the planner says to search next, with a short label, so it stands out among the ripe ones. */
class RouteOverlay extends Overlay
{
	/** Label written over the next vine. */
	static final String LABEL = "NEXT";

	private final SnakeWeedHelperConfig config;
	private final VineTracker tracker;
	private final RouteService route;
	private final BankReminder reminder;

	@Inject
	RouteOverlay(SnakeWeedHelperConfig config, VineTracker tracker, RouteService route, BankReminder reminder)
	{
		this.config = config;
		this.tracker = tracker;
		this.route = route;
		this.reminder = reminder;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WorldPoint tile = route.getNextVine();
		boolean hidden = !config.showNextVine() || reminder.hidesHighlights();
		GameObject object = tile == null || hidden ? null : tracker.object(tile);
		if (object == null)
			return null;

		Shape clickbox = object.getClickbox();
		HighlightStyle.drawRing(graphics, clickbox != null ? clickbox : object.getConvexHull(), config.targetColor());
		graphics.setFont(FontManager.getRunescapeSmallFont());
		Point location = object.getCanvasTextLocation(graphics, LABEL, 30);
		if (location != null)
			HighlightStyle.drawText(graphics, LABEL, location.getX(), location.getY(), Color.WHITE, 1);

		return null;
	}
}
