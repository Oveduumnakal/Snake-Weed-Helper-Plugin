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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests {@link HighlightStyle}'s pulse and color-scaling math, and the overlay's countdown text. */
public class HighlightStyleTest
{
	@Test
	public void pulseStartsAtZeroPeaksAtHalfPeriodAndIsPeriodic()
	{
		assertEquals(0, HighlightStyle.pulse(0, 1000), 1e-9);
		assertEquals(1, HighlightStyle.pulse(500, 1000), 1e-9);
		assertEquals(HighlightStyle.pulse(250, 1000), HighlightStyle.pulse(1250, 1000), 1e-9);
	}

	@Test
	public void zeroPeriodGivesASteadyGlow()
	{
		assertEquals(1, HighlightStyle.pulse(12345, 0), 1e-9);
	}

	@Test
	public void scaleMultipliesAlphaAndClamps()
	{
		Color color = new Color(10, 20, 30, 200);

		assertEquals(100, HighlightStyle.scale(color, 0.5).getAlpha());
		assertEquals(200, HighlightStyle.scale(color, 5).getAlpha());
		assertEquals(0, HighlightStyle.scale(color, -1).getAlpha());
		assertEquals(10, HighlightStyle.scale(color, 0.5).getRed());
	}

	@Test
	public void countdownRoundsUpToSecondsAndMarksEstimates()
	{
		Vine picked = new Vine(null, VineState.PICKED, 100);
		Vine guessed = new Vine(null, VineState.UNKNOWN, 100);

		assertEquals("60s", VineHighlightOverlay.label(picked, 0));
		assertEquals("~60s", VineHighlightOverlay.label(guessed, 0));
		assertEquals("1s", VineHighlightOverlay.label(picked, 99));
		assertEquals("...", VineHighlightOverlay.label(picked, 100));
		assertEquals("...", VineHighlightOverlay.label(picked, 400));
	}
}
