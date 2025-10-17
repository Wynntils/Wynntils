/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.mixin.accessors.ClientboundBossEventPacketAccessor;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.AddOperation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Operation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.UpdateNameOperation;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class AbbreviateMobHealthFeature extends Feature {
    private static final Pattern MOB_HEALTH_PATTERN =
            Pattern.compile("(.*§[cb])(?<current>\\d+)(§.(?<max>\\/\\d+))?(§[cb4]\\s?❤.*)");

    @SubscribeEvent
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();
        Operation operation = ((ClientboundBossEventPacketAccessor) packet).getOperation();

        if (operation instanceof AddOperation addOperation) {
            addOperation.name = transformHealthComponent(addOperation.name);
            return;
        }

        if (operation instanceof UpdateNameOperation updateOperation) {
            updateOperation.name = transformHealthComponent(updateOperation.name);
            return;
        }
    }

    private Component transformHealthComponent(Component component) {
        String name = component.getString();
        Matcher healthMatcher = MOB_HEALTH_PATTERN.matcher(name);
        if (!healthMatcher.matches()) return component;

        StyledText styledText = StyledText.fromComponent(component);

        StyledText modified = styledText.iterate((part, changes) -> {
            String partStr = part.getString(null, StyleType.NONE);

            try {
                String formattedHealth;
                if (partStr.equals(healthMatcher.group("current"))) {
                    int rawHealth = Integer.parseInt(partStr);
                    formattedHealth =
                            StringUtils.integerToShortString(rawHealth).toLowerCase(Locale.ROOT);
                } else if (partStr.equals(healthMatcher.group("max"))) {
                    int rawHealth = Integer.parseInt(partStr.substring(1));
                    formattedHealth =
                            "/" + StringUtils.integerToShortString(rawHealth).toLowerCase(Locale.ROOT);
                } else {
                    return IterationDecision.CONTINUE;
                }

                StyledTextPart newPart =
                        new StyledTextPart(formattedHealth, part.getPartStyle().getStyle(), null, Style.EMPTY);

                changes.remove(part);
                changes.add(newPart);
            } catch (NumberFormatException ignored) {
                return IterationDecision.CONTINUE;
            }

            return IterationDecision.CONTINUE;
        });

        if (modified.equals(styledText)) return component;

        return modified.getComponent();
    }
}
