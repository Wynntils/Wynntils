/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.SoulPointTimerFeature;
import com.wynntils.mc.utils.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SoulPointItemStack extends WynnItemStack {
    private final List<Component> tooltip;

    public SoulPointItemStack(ItemStack stack) {
        super(stack);

        tooltip = getOriginalTooltip();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        if (SoulPointTimerFeature.INSTANCE.isEnabled()) {
            List<Component> copy = new ArrayList<>(tooltip);

            copy.add(Component.literal(" "));

            int rawSecondsUntilSoulPoint = Managers.Character.getCharacterInfo().getTicksToNextSoulPoint() / 20;
            int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
            int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

            copy.add(Component.translatable(
                            "feature.wynntils.soulPointTimer.lore",
                            ChatFormatting.WHITE
                                    + String.format("%d:%02d", minutesUntilSoulPoint, secondsUntilSoulPoint))
                    .withStyle(ChatFormatting.AQUA));

            return copy;
        }

        return tooltip;
    }

    /**
     * @return Time in game ticks (1/20th of a second, 50ms) until next soul point
     *     <p>-1 if unable to determine
     */
    private static int getTicksTillNextSoulPoint() {
        if (McUtils.mc().level == null) return -1;

        return 24000 - (int) (McUtils.mc().level.getDayTime() % 24000);
    }
}
