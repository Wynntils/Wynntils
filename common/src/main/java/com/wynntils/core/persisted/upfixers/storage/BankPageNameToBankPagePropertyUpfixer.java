/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Set;

public class BankPageNameToBankPagePropertyUpfixer implements Upfixer {
    private static final String[] NORMAL_KEY_TEMPLATES = {
        "model.bank.customAccountBankPage%s",
        "model.bank.customBlockBankPage%s",
        "model.bank.customBookshelfPage%s",
        "model.bank.customMiscBucketPage%s"
    };

    private static final String CHARACTER_BANK_KEY_TEMPLATE = "model.bank.customCharacterBankPages%s";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        migrateNormalKeys(configObject);
        migrateCharacterBank(configObject);

        return true;
    }

    private static void migrateNormalKeys(JsonObject configObject) {
        for (String keyTemplate : NORMAL_KEY_TEMPLATES) {
            String nameKey = String.format(keyTemplate, "Names");

            if (configObject.has(nameKey)) {
                JsonObject names = configObject.get(nameKey).getAsJsonObject();

                String customizationKey = String.format(keyTemplate, "Customizations");

                configObject.add(customizationKey, createCustomizationsFromNames(names));
                configObject.remove(nameKey);
            }
        }
    }

    private static void migrateCharacterBank(JsonObject configObject) {
        String nameKey = String.format(CHARACTER_BANK_KEY_TEMPLATE, "Names");

        if (configObject.has(nameKey)) {
            JsonObject oldCharacters = configObject.get(nameKey).getAsJsonObject();
            var newCharacters = new JsonObject();

            for (String characterId : oldCharacters.keySet()) {
                var names = oldCharacters.get(characterId).getAsJsonObject();
                newCharacters.add(characterId, createCustomizationsFromNames(names));
            }

            String customizationKey = String.format(CHARACTER_BANK_KEY_TEMPLATE, "Customizations");

            configObject.add(customizationKey, newCharacters);
            configObject.remove(nameKey);
        }
    }

    private static JsonObject createCustomizationsFromNames(JsonObject names) {
        var customizations = new JsonObject();

        for (String pageIndex : names.keySet()) {
            var customName = names.get(pageIndex).getAsString();

            var obj = new JsonObject();
            obj.addProperty("name", customName);
            obj.addProperty("icon", "none");

            customizations.add(pageIndex, obj);
        }

        return customizations;
    }
}
