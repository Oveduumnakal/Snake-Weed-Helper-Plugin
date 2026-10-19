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

import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

/** {@link Requirements} read from the live client. Must be called on the client thread. */
@Singleton
class ClientRequirements implements Requirements
{
	private final Client client;

	@Inject
	ClientRequirements(Client client)
	{
		this.client = client;
	}

	@Override
	public int skillLevel(String skill)
	{
		String upper = skill.trim().toUpperCase(Locale.ROOT);
		String name = upper.replace(' ', '_');
		try
		{
			return client.getRealSkillLevel(Skill.valueOf(name));
		}
		catch (IllegalArgumentException e)
		{
			return 0;
		}
	}

	@Override
	public boolean questFinished(String quest)
	{
		for (Quest candidate : Quest.values())
		{
			if (candidate.getName().equalsIgnoreCase(quest.trim()))
				return candidate.getState(client) == QuestState.FINISHED;
		}

		return false;
	}

	@Override
	public int varbit(int id)
	{
		return client.getVarbitValue(id);
	}

	@Override
	public int varp(int id)
	{
		return client.getVarpValue(id);
	}
}
