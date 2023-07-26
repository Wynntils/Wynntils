/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.wynnlanguage;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.discoveries.DiscoveryInfo;
import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.screens.translation.WynnLanguage;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WynnLanguageService extends Manager {
    private static final StyledText GAVELLIAN_TRANSCRIBER = StyledText.fromString("§rHigh Gavellian Transcriber");
    private static final StyledText WYNNIC_TRANSCRIBER = StyledText.fromString("§fAncient Wynnic Transcriber");
    private static final String GAVELLIAN_TRANSCRIBER_DISCOVERY = "Ne du Valeos du Ellach";
    private static final String WYNNIC_TRANSCRIBER_DISCOVERY = "Wynn Plains Monument";

    private WynnLanguage selectedLanguage = WynnLanguage.DEFAULT;

    public WynnLanguageService() {
        super(List.of());
    }

    public void setSelectedLanguage(WynnLanguage selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public WynnLanguage getSelectedLanguage() {
        return selectedLanguage;
    }

    public boolean hasTranscriber(WynnLanguage transciberToFind) {
        Inventory inventory = McUtils.inventory();

        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);

            if (transciberToFind == WynnLanguage.WYNNIC) {
                if (StyledText.fromComponent(itemStack.getHoverName()).equals(WYNNIC_TRANSCRIBER)) {
                    return true;
                }
            } else {
                if (StyledText.fromComponent(itemStack.getHoverName()).equals(GAVELLIAN_TRANSCRIBER)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean completedDiscovery(WynnLanguage discoveryToCheck) {
        Stream<DiscoveryInfo> discoveryInfoStream =
                Models.Discovery.getAllCompletedDiscoveries(ActivitySortOrder.ALPHABETIC);

        String nameToFind = discoveryToCheck == WynnLanguage.WYNNIC
                ? WYNNIC_TRANSCRIBER_DISCOVERY
                : GAVELLIAN_TRANSCRIBER_DISCOVERY;

        Optional<DiscoveryInfo> foundDiscoveryInfo = discoveryInfoStream
                .filter(discoveryInfo -> discoveryInfo.getName().equals(nameToFind))
                .findFirst();

        return foundDiscoveryInfo.map(DiscoveryInfo::isDiscovered).orElse(false);
    }
}
