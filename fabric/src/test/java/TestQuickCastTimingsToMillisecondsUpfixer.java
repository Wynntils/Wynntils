/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.upfixers.config.QuickCastTimingsToMillisecondsUpfixer;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestQuickCastTimingsToMillisecondsUpfixer {
    @Test
    public void migratesLegacyTickDelaysAndRemovesObsoleteKeys() {
        JsonObject configObject = new JsonObject();
        configObject.addProperty("quickCastFeature.leftClickTickDelay", 3);
        configObject.addProperty("quickCastFeature.rightClickTickDelay", 4);
        configObject.addProperty("quickCastFeature.spellCooldown", 2);
        configObject.addProperty("quickCastFeature.blockAttacks", true);
        configObject.addProperty("quickCastFeature.safeCasting", "BLOCK_ALL");

        boolean changed = new QuickCastTimingsToMillisecondsUpfixer().apply(configObject, Set.of());

        Assertions.assertTrue(changed);
        Assertions.assertEquals(
                150, configObject.get("quickCastFeature.leftClickDelayMs").getAsInt());
        Assertions.assertEquals(
                200, configObject.get("quickCastFeature.rightClickDelayMs").getAsInt());
        Assertions.assertEquals(
                100, configObject.get("quickCastFeature.spellCooldownMs").getAsInt());
        Assertions.assertFalse(configObject.has("quickCastFeature.leftClickTickDelay"));
        Assertions.assertFalse(configObject.has("quickCastFeature.rightClickTickDelay"));
        Assertions.assertFalse(configObject.has("quickCastFeature.spellCooldown"));
        Assertions.assertFalse(configObject.has("quickCastFeature.blockAttacks"));
        Assertions.assertFalse(configObject.has("quickCastFeature.safeCasting"));
    }

    @Test
    public void keepsExistingMillisecondValues() {
        JsonObject configObject = new JsonObject();
        configObject.addProperty("quickCastFeature.leftClickTickDelay", 3);
        configObject.addProperty("quickCastFeature.leftClickDelayMs", 90);

        boolean changed = new QuickCastTimingsToMillisecondsUpfixer().apply(configObject, Set.of());

        Assertions.assertTrue(changed);
        Assertions.assertEquals(
                90, configObject.get("quickCastFeature.leftClickDelayMs").getAsInt());
        Assertions.assertFalse(configObject.has("quickCastFeature.leftClickTickDelay"));
    }
}
