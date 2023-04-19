/*
 * Copyright (c) 2018, Joris K <kjorisje@gmail.com>
 * Copyright (c) 2018, Lasse <cronick@zytex.dk>
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
package com.gimimprove;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("GIM Improve")
public interface GimImproveConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "splitGroupChat",
			name = "Split group chat with private",
			description = "Configures the group chat to be returned in private chat and has the chat's split"
	)
	default boolean splitGroupChat()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "syncGIMLogs",
			name = "Sync GIM Collection Logs",
			description = "Configures your collection log to include your GIM's collection log"
	)
	default boolean syncGIMLogs()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "detectLogUniqueToGroup",
			name = "Detect unlock unique to group",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default boolean detectLogUniqueToGroup() { return true; }

	@ConfigItem(
			position = 4,
			keyName = "users",
			name = "Comma-separated usernames",
			description = "Comma-separated list of users (max 5) to use instead of GIM group. Will ignore GIM group if this is populated."
	)
	default String users() { return ""; }

	@ConfigItem(
			position = 5,
			keyName = "color1",
			name = "User 1 color",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default Color color1() { return new Color(203, 123, 255); }
	@ConfigItem(
			position = 6,
			keyName = "color2",
			name = "User 2 color",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default Color color2() { return new Color(250, 168, 64); }
	@ConfigItem(
			position = 7,
			keyName = "color3",
			name = "User 3 color",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default Color color3() { return new Color(125, 255, 89); }
	@ConfigItem(
			position = 8,
			keyName = "color4",
			name = "User 4 color",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default Color color4() { return new Color(89, 177, 238); }
	@ConfigItem(
			position = 9,
			keyName = "color5",
			name = "User 5 color",
			description = "Detect if a collection log unlock is unique the GIM group"
	)
	default Color color5() { return new Color(248, 108, 108); }
}
