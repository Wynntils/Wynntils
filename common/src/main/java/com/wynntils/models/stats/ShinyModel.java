/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.ShinyStatType;
import com.wynntils.utils.mc.McUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public final class ShinyModel extends Model {
    private Map<String, ShinyStatType> shinyStatTypes = new HashMap<>();

    public ShinyModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_SHINY_STATS).handleReader(this::handleShinyStatTypes);
    }

    private void handleShinyStatTypes(Reader reader) {
        Type type = new TypeToken<List<ShinyStatType>>() {}.getType();
        List<ShinyStatType> statTypes = Managers.Json.GSON.fromJson(reader, type);

        shinyStatTypes = statTypes.stream().collect(Collectors.toMap(ShinyStatType::displayName, statType -> statType));
    }

    public ShinyStatType getShinyStatType(int id) {
        return shinyStatTypes.values().stream()
                .filter(statType -> statType.id() == id)
                .findFirst()
                .orElse(ShinyStatType.UNKNOWN);
    }

    public Optional<ShinyStat> getShinyStat(ItemStack itemStack) {
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isEmpty()) return Optional.empty();

        Optional<GearInstance> gearInstanceOpt = gearItemOpt.get().getItemInstance();
        if (gearInstanceOpt.isEmpty()) return Optional.empty();

        return gearInstanceOpt.get().shinyStat();
    }

    public List<ShinyStat> getAllShinyStats() {
        List<ShinyStat> allShinies = new ArrayList<>();
        int size = McUtils.inventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = McUtils.inventory().getItem(i);
            Optional<ShinyStat> shinyOpt = getShinyStat(itemStack);
            if (shinyOpt.isPresent()) {
                allShinies.add(shinyOpt.get());
            }
        }

        return allShinies;
    }

    public ShinyStatType getShinyStat(String displayName) {
        ShinyStatType shinyStatType = shinyStatTypes.get(displayName);

        if (shinyStatType == null) {
            WynntilsMod.warn("Unknown shiny stat type: " + displayName);
            return ShinyStatType.UNKNOWN;
        }

        return shinyStatType;
    }
}
