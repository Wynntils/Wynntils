/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.profession.label.ProfessionGatheringNodeLabelInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class HighlightGatheringNodesFeature extends Feature {
    @Persisted
    private final Config<CustomColor> highlightColor = new Config<>(CommonColors.WHITE);

    @Persisted
    private final Config<Boolean> alwaysShow = new Config<>(false);

    public HighlightGatheringNodesFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelEvent.Pre event) {
        Stream<ProfessionGatheringNodeLabelInfo> nodes = Models.Profession.getNodesSet().stream();

        if (!alwaysShow.get()) {
            Optional<GatheringToolItem> heldItem =
                    Models.Item.asWynnItem(McUtils.player().getMainHandItem(), GatheringToolItem.class);
            if (heldItem.isEmpty()) return;

            nodes = nodes.filter(node -> heldItem.get()
                    .getProfessionTypes()
                    .contains(node.getMaterialType().getProfessionType()));
        }

        nodes.forEach(node -> {
            BlockPos pos = node.getLocation().toBlockPos();
            switch (node.getMaterialType().getProfessionType()) {
                case MINING, FISHING -> pos = pos.below();
                case FARMING -> pos = pos.below(2);
            }
            Gizmos.cuboid(pos, GizmoStyle.stroke(highlightColor.get().asInt()));
        });
    }
}
