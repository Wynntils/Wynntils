/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides;

import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import com.wynntils.wynn.utils.WynnItemUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGearItemStack extends GuideItemStack {
    private final List<Component> generatedTooltip;
    private final MutableComponent name;
    private final GearProfile gearProfile;

    public GuideGearItemStack(GearProfile gearProfile) {
        super(gearProfile.getGearInfo().asItemStack());
        this.gearProfile = gearProfile;

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        if (gearProfile.getGearInfo().isArmorColorValid())
            tag.putInt("color", gearProfile.getGearInfo().getArmorColorAsInt());
        this.setTag(tag);

        name = Component.literal(gearProfile.getDisplayName())
                .withStyle(gearProfile.getTier().getChatFormatting());

        this.generatedTooltip = GearTooltipBuilder.fromGearProfile(gearProfile)
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

    public GearProfile getGearProfile() {
        return gearProfile;
    }
}
