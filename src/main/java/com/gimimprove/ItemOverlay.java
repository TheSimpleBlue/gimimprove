package com.gimimprove;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.widgets.WidgetID.COLLECTION_LOG_ID;

public class ItemOverlay extends WidgetItemOverlay
{
    private final Client client;
    private final GimImprovePlugin plugin;
    private final ItemManager itemManager;
    private final TooltipManager tooltipManager;

    @Getter
    private final Cache<Integer, BufferedImage> wastedSpaceImages = CacheBuilder.newBuilder()
            .maximumSize(160)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    @Inject
    ItemOverlay(Client client, GimImprovePlugin plugin, ItemManager itemManager, TooltipManager tooltipManager)
    {
        this.client = client;
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.tooltipManager = tooltipManager;
        showOnInterfaces(COLLECTION_LOG_ID);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        Set<Integer> items =  plugin.getUnlockedItemIDSet();
        if (items.isEmpty() || !items.contains(itemId))
        {
            return;
        }

        UnlockedItem item = plugin.getUnlockedGroupItemsMap().get(itemId);
        Rectangle bounds = itemWidget.getCanvasBounds();

        if (bounds.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
        {
            Tooltip t = new Tooltip(ColorUtil.prependColorTag("Already acquired by: " + String.join(", " , item.unlockedByUsername), new Color(238, 238, 238)));
            tooltipManager.add(t);
        }

        int i = 0;
        for (String username : item.unlockedByUsername) {
            if (plugin.getUserMap().containsKey(username)) {
                graphics.setColor(plugin.getUserMap().get(username).color);
                int x = bounds.x + bounds.width - 8;
                int y = bounds.y + bounds.height - 8 - 6 * i;
                graphics.fillOval(x, y, 6, 6);
                i++;
            }
        }
    }
}