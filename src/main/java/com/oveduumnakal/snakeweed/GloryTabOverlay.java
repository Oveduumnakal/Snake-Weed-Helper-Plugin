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
import java.awt.Rectangle;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * While the "Time to Bank" marker shows, points the player at their amulet of glory: glows the Worn Equipment tab
 * when it is closed, and glows the amulet in the neck slot once it is open.
 */
class GloryTabOverlay extends Overlay
{
	/** The Worn Equipment tab button in each interface layout (fixed, resizable, stretch, pre-EoC). */
	private static final int[] EQUIPMENT_TABS =
	{
		InterfaceID.Toplevel.STONE4,
		InterfaceID.ToplevelOsm.STONE4,
		InterfaceID.ToplevelOsrsStretch.STONE4,
		InterfaceID.ToplevelPreEoc.STONE4,
	};

	/** Text every glory amulet name contains, charged or not. */
	private static final String GLORY = "glory";

	private final Client client;
	private final SnakeWeedHelperConfig config;
	private final BankReminder reminder;

	@Inject
	GloryTabOverlay(Client client, SnakeWeedHelperConfig config, BankReminder reminder)
	{
		this.client = client;
		this.config = config;
		this.reminder = reminder;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!reminder.showsMarker() || !wearingGlory())
			return null;

		Widget neck = client.getWidget(InterfaceID.Wornitems.SLOT2);
		Widget target = neck != null && !neck.isHidden() ? neck : equipmentTab();
		if (target == null)
			return null;

		Color color = config.bankColor();
		Rectangle bounds = target.getBounds();
		HighlightStyle.draw(graphics, bounds, color, HighlightStyle.pulse(System.currentTimeMillis(),
			BankMarkerOverlay.PULSE_MILLIS));
		return null;
	}

	/** The Worn Equipment tab button of the layout in use, or null when none is showing. */
	private Widget equipmentTab()
	{
		for (int id : EQUIPMENT_TABS)
		{
			Widget tab = client.getWidget(id);
			if (tab != null && !tab.isHidden())
				return tab;
		}

		return null;
	}

	/** Whether the neck slot holds an amulet of glory. */
	private boolean wearingGlory()
	{
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);
		Item amulet = worn == null ? null : worn.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
		if (amulet == null || amulet.getId() <= 0)
			return false;

		ItemComposition item = client.getItemDefinition(amulet.getId());
		String name = item.getName().toLowerCase();
		return name.contains(GLORY);
	}
}
