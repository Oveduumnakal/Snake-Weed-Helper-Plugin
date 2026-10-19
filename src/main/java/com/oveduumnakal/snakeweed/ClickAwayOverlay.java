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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Tells the player to click away on the one tick they have after a failed 3-tick search, in large text near the top
 * third of the game screen, and warns for a few seconds when the window was missed.
 */
class ClickAwayOverlay extends Overlay
{
	/** The prompt shown on the decision tick. */
	static final String PROMPT = "NO HERB: CLICK AWAY";

	/** The warning shown after the manual search began. */
	static final String MISSED = "Missed: manual search started";

	private static final float PROMPT_SIZE = 44f;
	private static final float MISSED_SIZE = 22f;
	private static final Color MISSED_COLOR = new Color(255, 170, 40);

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final SearchService search;

	@Inject
	ClickAwayOverlay(Client client, SnakeWeedHelperConfig config, SearchService search)
	{
		this.client = client;
		this.config = config;
		this.search = search;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		SearchPhase phase = search.phase();
		if (phase == SearchPhase.DECISION && config.clickAwayPrompt())
			drawCentered(graphics, PROMPT, PROMPT_SIZE, config.clickAwayColor());
		else if (phase == SearchPhase.MISSED && config.missedWarning())
			drawCentered(graphics, MISSED + " (" + search.missed() + ")", MISSED_SIZE, MISSED_COLOR);

		return null;
	}

	/** Writes text centred across the game screen, a third of the way down. */
	private void drawCentered(Graphics2D graphics, String text, float size, Color color)
	{
		graphics.setFont(FontManager.getRunescapeBoldFont().deriveFont(size));
		FontMetrics metrics = graphics.getFontMetrics();
		int x = (client.getCanvasWidth() - metrics.stringWidth(text)) / 2;
		HighlightStyle.drawText(graphics, text, x, client.getCanvasHeight() / 3, color, 1);
	}
}
