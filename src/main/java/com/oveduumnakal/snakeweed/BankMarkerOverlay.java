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
import java.awt.Polygon;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Glows the tile under the player, with the label "Time to Bank", while the backpack is full at the vines. It is
 * drawn where the player is each frame, so it stays under them as they walk.
 */
class BankMarkerOverlay extends Overlay
{
	/** Label written over the tile. */
	static final String LABEL = "Time to Bank";

	/** Length of one glow pulse in milliseconds. */
	static final int PULSE_MILLIS = 1200;

	/** Height in local units the label clears a player's head by, on top of the player's height. */
	private static final int LABEL_LIFT = 90;

	/** Least player height assumed in local units, for when the client reports a smaller logical height. */
	private static final int MIN_PLAYER_HEIGHT = 190;

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final BankReminder reminder;

	@Inject
	BankMarkerOverlay(Client client, SnakeWeedHelperConfig config, BankReminder reminder)
	{
		this.client = client;
		this.config = config;
		this.reminder = reminder;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Player me = client.getLocalPlayer();
		if (me == null || !reminder.showsMarker())
			return null;

		LocalPoint where = me.getLocalLocation();
		Polygon tile = Perspective.getCanvasTilePoly(client, where);
		Color color = config.bankColor();
		if (tile != null)
			HighlightStyle.draw(graphics, tile, color, HighlightStyle.pulse(System.currentTimeMillis(), PULSE_MILLIS));

		graphics.setFont(FontManager.getRunescapeBoldFont());
		Point text = Perspective.getCanvasTextLocation(client, graphics, where, LABEL,
			Math.max(me.getLogicalHeight(), MIN_PLAYER_HEIGHT) + LABEL_LIFT);
		if (text != null)
			HighlightStyle.drawText(graphics, LABEL, text.getX(), text.getY(), color, 1);

		return null;
	}
}
