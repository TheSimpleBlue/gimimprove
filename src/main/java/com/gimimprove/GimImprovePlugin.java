/*
 * Copyright (c) 2019, Lotto <https://github.com/devLotto>
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

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import okhttp3.*;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
	name = "GIM Improve",
	description = "Group Iron Man Quality of Life Improvements - puts GIM chat into PMs and GIM team collection log indicators"
)
@Slf4j
public class GimImprovePlugin extends Plugin
{
	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log: (.*)");
	private String obtainedItemName;
	@Inject
	private Client client;
	@Inject
	private ItemManager itemManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemOverlay storageItemOverlay;
	@Inject
	private GimImproveConfig config;
	@Inject
	private OkHttpClient okHttpClient;

	@Provides
	GimImproveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GimImproveConfig.class);
	}

	private static Map<Integer, UnlockedItem> ITEM_ID_MAP = new HashMap<>();

	private static Map<String, User> USER_MAP = new HashMap<>();

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(storageItemOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(storageItemOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case LOADING:
				break;
			case LOGGED_IN:
				updateData();
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
			Matcher m = COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage());
			if (m.matches()) {
				updateData();
				obtainedItemName = Text.removeTags(m.group(1));
				int itemID = 0;

				if (!itemManager.search(obtainedItemName).isEmpty()) {
					itemID = itemManager.search(obtainedItemName).get(0).getId();
				} else {
					// Should not happen, but this is a 'hacky' but effective way to do it.
				}
				if (ITEM_ID_MAP.containsKey(itemID) && config.detectLogUniqueToGroup()) {
					client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							chatMessage.getName(),
							"Item is not unique item for your group, already unlocked by: " + String.join(", ", ITEM_ID_MAP.get(itemID).unlockedByUsername),
							chatMessage.getSender());
				}
			}
		}

		if (config.splitGroupChat() && chatMessage.getType() == ChatMessageType.CLAN_GIM_CHAT) {
			client.addChatMessage(
					ChatMessageType.PRIVATECHAT,
					chatMessage.getName(),
					chatMessage.getMessage(),
					chatMessage.getSender());
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == WidgetID.ADVENTURE_LOG_ID)
		{
			Widget adventureLog = client.getWidget(WidgetInfo.ADVENTURE_LOG);
			if (adventureLog == null)
			{
				return;
			}
		}
		if (widgetLoaded.getGroupId() == WidgetID.COLLECTION_LOG_ID)
		{
			Widget collectionLog = client.getWidget(WidgetInfo.COLLECTION_LOG);
			if (collectionLog != null)
			{
				updateData();
			}

		}
	}

	public void updateData() {
		buildUsers();
		try {
			for (String user : getUserMap().keySet()) {
			Set<Integer> items = getCollectionLogItemIdSet(user);
				for (Integer id : items){
					addItemForUser(id, user);
				}
			}
		} catch(Exception e) {
			// rip.
		}
	}

	public void addItemForUser(int itemID, String username) {
		if (!ITEM_ID_MAP.containsKey(itemID)) {
			ITEM_ID_MAP.put(itemID, new UnlockedItem(itemID));
		}
		ITEM_ID_MAP.get(itemID).addUser(username);
	}

	private void buildUsers() {
		List<Color> colorList =
				Arrays.asList(config.color1(), config.color2(), config.color3(), config.color4(), config.color5());
		USER_MAP.clear();
		Set<String> users = new HashSet<>();
		if (!config.users().isEmpty()) {
			List<String> results = Splitter.on(",").trimResults().splitToList(config.users());
			users.addAll(results);
		} else {
			ClanSettings groupIronClan = client.getClanSettings(1);
			if (groupIronClan != null) {
				for (ClanMember member : groupIronClan.getMembers()) {
					users.add(member.getName());
				}
			}
		}
		int i = 0;
		for (String username : users) {
			USER_MAP.put(username, new User(username, colorList.get(i)));
			i++;
		}
	}

	public Map<Integer, UnlockedItem> getUnlockedGroupItemsMap()
	{
		return ITEM_ID_MAP;
	}

	public Map<String, User> getUserMap()
	{
		return USER_MAP;
	}
	public Set<Integer> getUnlockedItemIDSet()
	{
		return ITEM_ID_MAP.keySet();
	}


	private static final String COLLECTION_LOG_API_BASE = "api.collectionlog.net";
	private static final String COLLECTION_LOG_USER_PATH = "user";
	private static final String COLLECTION_LOG_LOG_PATH = "collectionlog";
	private static final String COLLECTION_LOG_USER_AGENT = "Runelite collection-log/2.2";
	public Set<Integer> getCollectionLogItemIdSet(String username) throws IOException
	{
		HttpUrl url = new HttpUrl.Builder()
				.scheme("https")
				.host(COLLECTION_LOG_API_BASE)
				.addPathSegment(COLLECTION_LOG_LOG_PATH)
				.addPathSegment(COLLECTION_LOG_USER_PATH)
				.addEncodedPathSegment(username)
				.build();

		return getCollectionLogItemIdSet(getRequest(url));
	}

	private Set<Integer> getCollectionLogItemIdSet(JsonObject json) {
		Set<Integer> itemIdSet = new HashSet<>();
		if (json == null) {
			return itemIdSet;
		}
		JsonObject jsonObjectTabs = json
				.getAsJsonObject()
				.get("collectionLog")
				.getAsJsonObject()
				.get("tabs")
				.getAsJsonObject();

		for (String tabKey : jsonObjectTabs.keySet())
		{
			JsonObject tab = jsonObjectTabs.get(tabKey).getAsJsonObject();
			for (String pageKey : tab.keySet())
			{
				JsonObject page = tab.get(pageKey).getAsJsonObject();
				for (JsonElement item : page.get("items").getAsJsonArray())
				{
					if(item.getAsJsonObject().get("obtained").getAsBoolean()) {
						itemIdSet.add(item.getAsJsonObject().get("id").getAsInt());
					}
				}
			}
		}
		return itemIdSet;
	}

	private JsonObject getRequest(HttpUrl url) throws IOException
	{
		Request request = new Request.Builder()
				.header("User-Agent", COLLECTION_LOG_USER_AGENT)
				.url(url)
				.get()
				.build();

		return apiRequest(request);
	}

	private JsonObject apiRequest(Request request) throws IOException
	{
		if (!config.syncGIMLogs())
		{
			return null;
		}

		Response response = okHttpClient.newCall(request).execute();
		JsonObject responseJson = processResponse(response);
		response.close();
		return responseJson;
	}

	private JsonObject processResponse(Response response) throws IOException
	{
		if (!response.isSuccessful())
		{
			return null;
		}

		ResponseBody resBody = response.body();
		if (resBody == null)
		{
			return null;
		}
		return new JsonParser().parse(resBody.string()).getAsJsonObject();
	}
}
