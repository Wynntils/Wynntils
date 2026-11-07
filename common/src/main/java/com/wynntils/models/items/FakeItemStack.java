/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class FakeItemStack extends ItemStack {
    private final WynnItem wynnItem;
    private final ItemStack itemStack;
    private final ItemSourceType sourceType;
    private final String source;

    private int tooltipPage;
    private TooltipBuilder tooltipBuilder;

    public FakeItemStack(WynnItem wynnItem, ItemStack itemStack, ItemSourceType sourceType, String source) {
        super(itemStack.getItem(), 1);
        this.applyComponents(itemStack.getComponentsPatch());

        if (wynnItem instanceof NamedItemProperty namedItemProperty) {
            Handlers.Item.updateItem(this, wynnItem, StyledText.fromString(namedItemProperty.getName()));
        }
        if (wynnItem instanceof GearTierItemProperty tierItem) {
            boolean shiny = wynnItem instanceof ShinyItemProperty shinyItem
                    && shinyItem.getShinyStat().isPresent();
            this.set(DataComponents.TOOLTIP_STYLE, tierItem.getGearTier().getTooltipStyle(shiny));
        }

        this.wynnItem = wynnItem;
        this.itemStack = itemStack;
        this.sourceType = sourceType;
        this.source = source;
    }

    public FakeItemStack(GearItem gearItem, ItemSourceType sourceType, String source) {
        this(gearItem, gearItem.getItemInfo().metaInfo().material().itemStack(), sourceType, source);
    }

    public FakeItemStack(TomeItem tomeItem, ItemSourceType sourceType, String source) {
        this(tomeItem, tomeItem.getItemInfo().metaInfo().material().itemStack(), sourceType, source);
    }

    // This should be only used by chat items, so the item does not matter
    public FakeItemStack(WynnItem wynnItem, ItemSourceType sourceType, String source) {
        this(wynnItem, new ItemStack(Items.STONE), sourceType, source);
    }

    public String getSource() {
        return source;
    }

    public ItemSourceType getSourceType() {
        return sourceType;
    }

    @Override
    public ItemStack copy() {
        FakeItemStack copy = new FakeItemStack(wynnItem, itemStack, sourceType, source);
        copy.tooltipPage = tooltipPage;
        return copy;
    }

    public boolean hasTooltipPages() {
        return wynnItem instanceof PagedItemProperty;
    }

    public int getTooltipPage() {
        return tooltipPage;
    }

    public boolean cyclePage() {
        if (!hasTooltipPages()) return false;

        tooltipPage = (tooltipPage + 1) % 3;
        tooltipBuilder = null;

        return true;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        try {
            return getWynnTooltip();
        } catch (Throwable t) {
            WynntilsMod.reportCrash(
                    CrashType.TOOLTIP,
                    this.getClass().getSimpleName(),
                    wynnItem.getClass().getName(),
                    "calculation",
                    false,
                    false,
                    t);

            return super.getTooltipLines(context, player, isAdvanced);
        }
    }

    public List<Component> getWynnTooltip() {
        if (tooltipBuilder == null) {
            if (wynnItem instanceof IdentifiableItemProperty<?, ?> identifiableItem) {
                tooltipBuilder = Handlers.Tooltip.buildNew(identifiableItem, sourceType, source, tooltipPage);
            } else if (wynnItem instanceof CraftedGearItem craftedGearItem) {
                tooltipBuilder = Handlers.Tooltip.buildNew(craftedGearItem, sourceType, source, tooltipPage);
            } else if (wynnItem instanceof CraftedConsumableItem craftedConsumableItem) {
                tooltipBuilder = Handlers.Tooltip.buildNew(craftedConsumableItem, sourceType, source);
            } else if (wynnItem instanceof MountItem mountItem) {
                tooltipBuilder = Handlers.Tooltip.buildNew(mountItem, sourceType, source);
            }
        }

        return tooltipBuilder == null ? List.of() : tooltipBuilder.getTooltipLines(Models.Character.getClassType());
    }
}
