/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.wynnlanguage;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.discoveries.DiscoveryInfo;
import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.utils.mc.McUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WynnLanguageService extends Service {
    private static final int FIFTY_INDEX = 10;
    private static final int ONE_HUNDERED_INDEX = 11;
    private static final int TEN_INDEX = 9;
    private static final List<Character> englishCharacters = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '.', '!', '?');
    private static final List<Integer> englishNumbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 50, 100);
    private static final List<Character> gavellianCharacters = List.of(
            'ⓐ', 'ⓑ', 'ⓒ', 'ⓓ', 'ⓔ', 'ⓕ', 'ⓖ', 'ⓗ', 'ⓘ', 'ⓙ', 'ⓚ', 'ⓛ', 'ⓜ', 'ⓝ', 'ⓞ', 'ⓟ', 'ⓠ', 'ⓡ', 'ⓢ', 'ⓣ', 'ⓤ',
            'ⓥ', 'ⓦ', 'ⓧ', 'ⓨ', 'ⓩ');
    private static final List<Character> wynnicCharacters = List.of(
            '⒜', '⒝', '⒞', '⒟', '⒠', '⒡', '⒢', '⒣', '⒤', '⒥', '⒦', '⒧', '⒨', '⒩', '⒪', '⒫', '⒬', '⒭', '⒮', '⒯', '⒰',
            '⒱', '⒲', '⒳', '⒴', '⒵', '０', '１', '２');
    private static final List<Character> wynnicNumbers = List.of('⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿');
    private static final Map<Character, Character> englishToGavellianMap = new HashMap<>();
    private static final Map<Character, Character> englishToWynnicMap = new HashMap<>();
    private static final Map<Character, Character> gavellianToEnglishMap = new HashMap<>();
    private static final Map<Character, Character> wynnicToEnglishMap = new HashMap<>();
    private static final StyledText GAVELLIAN_TRANSCRIBER = StyledText.fromString("§rHigh Gavellian Transcriber");
    private static final StyledText WYNNIC_TRANSCRIBER = StyledText.fromString("§fAncient Wynnic Transcriber");
    private static final String GAVELLIAN_TRANSCRIBER_DISCOVERY = "Ne du Valeos du Ellach";
    private static final String WYNNIC_TRANSCRIBER_DISCOVERY = "Wynn Plains Monument";

    private WynnLanguage selectedLanguage = WynnLanguage.DEFAULT;

    public WynnLanguageService() {
        super(List.of());

        createTranslationMaps();
    }

    private void createTranslationMaps() {
        for (int i = 0; i < gavellianCharacters.size(); i++) {
            gavellianToEnglishMap.put(
                    gavellianCharacters.get(i), englishCharacters.get(i));
        }

        for (int i = 0; i < wynnicCharacters.size(); i++) {
            wynnicToEnglishMap.put(wynnicCharacters.get(i), englishCharacters.get(i));
        }

        for (int i = 0; i < gavellianCharacters.size(); i++) {
            englishToGavellianMap.put(
                    englishCharacters.get(i), gavellianCharacters.get(i));
        }

        for (int i = 0; i < wynnicCharacters.size(); i++) {
            englishToWynnicMap.put(englishCharacters.get(i), wynnicCharacters.get(i));
        }
    }

    public List<Character> getGavellianCharacters() {
        return gavellianCharacters;
    }

    public List<Character> getWynnicCharacters() {
        return wynnicCharacters;
    }

    public List<Character> getEnglishCharacters() {
        return englishCharacters;
    }

    public List<Integer> getEnglishNumbers() {
        return englishNumbers;
    }

    public List<Character> getWynnicNumbers() {
        return wynnicNumbers;
    }

    public Character translateGavellianToEnglish(Character characterToTranslate) {
        return gavellianToEnglishMap.getOrDefault(characterToTranslate, characterToTranslate);
    }

    public Character translateWynnicToEnglish(Character characterToTranslate) {
        return wynnicToEnglishMap.getOrDefault(characterToTranslate, characterToTranslate);
    }

    public Character translateEnglishToGavellian(Character characterToTranslate) {
        return englishToGavellianMap.getOrDefault(characterToTranslate, characterToTranslate);
    }

    public Character translateEnglishToWynnic(Character characterToTranslate) {
        return englishToWynnicMap.getOrDefault(characterToTranslate, characterToTranslate);
    }

    public void setSelectedLanguage(WynnLanguage selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public WynnLanguage getSelectedLanguage() {
        return selectedLanguage;
    }

    public Character getFifty() {
        return wynnicNumbers.get(FIFTY_INDEX);
    }

    public Character getOneHundered() {
        return wynnicNumbers.get(ONE_HUNDERED_INDEX);
    }

    public Character getTen() {
        return wynnicNumbers.get(TEN_INDEX);
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
