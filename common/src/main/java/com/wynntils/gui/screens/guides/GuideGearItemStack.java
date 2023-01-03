/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides;

import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import com.wynntils.wynn.utils.WynnItemUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public class GuideGearItemStack extends GuideItemStack {
    private final List<Component> generatedTooltip;
    private final MutableComponent name;
    private final ItemProfile itemProfile;

    public GuideGearItemStack(ItemProfile itemProfile) {
        super(itemProfile.getItemInfo().asItemStack());
        this.itemProfile = itemProfile;

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        if (itemProfile.getItemInfo().isArmorColorValid())
            tag.putInt("color", itemProfile.getItemInfo().getArmorColorAsInt());
        this.setTag(tag);

        name = Component.literal(itemProfile.getDisplayName())
                .withStyle(itemProfile.getTier().getChatFormatting());

        this.generatedTooltip = GearTooltipBuilder.fromItemProfile(itemProfile)
                .getTooltipLines(WynnItemUtils.getCurrentIdentificationStyle());
    }

    @Override
    public Component getHoverName() {
        return name;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.addAll(generatedTooltip);

        return tooltip;
    }

    public ItemProfile getItemProfile() {
        return itemProfile;
    }
}
