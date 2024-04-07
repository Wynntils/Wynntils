/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

// NOTE: This feature was intented to be used on fully reset ability trees.
//       Although support for parsing any tree is present,
//       a fresh tree must be used for the best results.
@StartDisabled
@ConfigCategory(Category.DEBUG)
public class AbilityTreeDataDumpFeature extends Feature {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");

    @RegisterKeyBind
    public KeyBind dumpAbilityTree = new KeyBind(
            "Dump Ability Tree",
            GLFW.GLFW_KEY_0,
            true,
            () -> Models.AbilityTree.ABILITY_TREE_CONTAINER_QUERIES.dumpAbilityTree(this::saveToDisk));

    private void saveToDisk(AbilityTreeInfo abilityTreeInfo) {
        // Save the dump to a file
        JsonElement element = Managers.Json.GSON.toJsonTree(abilityTreeInfo);

        String fileName = Models.Character.getClassType().getName().toLowerCase(Locale.ROOT) + "_abilities.json";
        File jsonFile = new File(SAVE_FOLDER, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved ability tree dump to " + jsonFile.getAbsolutePath()));
    }
}
