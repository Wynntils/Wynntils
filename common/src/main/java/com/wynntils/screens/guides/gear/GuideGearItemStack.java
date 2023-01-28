/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.GearTooltipBuilder;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.wynn.WynnItemUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGearItemStack extends GuideItemStack {
    private final List<Component> generatedTooltip;
    private final MutableComponent name;
    private final GearInfo gearInfo;

    public GuideGearItemStack(GearInfo gearInfo) {
        super(gearInfo.metaInfo().material().asItemStack());
        this.gearInfo = gearInfo;
        ItemStack itemStack = gearInfo.metaInfo().material().asItemStack();

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        if (gearInfo.metaInfo().material().hasColorCode()) {
            tag.putInt("color", gearInfo.metaInfo().material().getColorCode());
        }
        this.setTag(tag);

        name = Component.literal(gearInfo.name()).withStyle(gearInfo.tier().getChatFormatting());

        this.generatedTooltip = GearTooltipBuilder.fromGearInfo(gearInfo)
                .getTooltipLines(WynnItemUtils.getCurrentIdentificationStyle());
    }

    @Override
    public Component getHoverName() {
        return name;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        return new ArrayList<>(generatedTooltip);
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }
}
