/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.keybinds.KeyBindManager;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestQuickCastKeyPresses {
    private KeyMapping mapping;
    private InputConstants.Key key;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void createMapping() {
        key = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F8);
        mapping = new KeyMapping(
                "testQuickCastPress", InputConstants.Type.KEYSYM, key.getValue(), KeyBindManager.COMBAT_CATEGORY);
        KeyMapping.resetMapping();
    }

    @AfterEach
    public void releaseMapping() {
        KeyMapping.releaseAll();
    }

    @Test
    public void recognizesKeyboardPressReleasedBeforeRefresh() {
        pressAndRelease();

        Assertions.assertFalse(mapping.isDown());
        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, false));
    }

    @Test
    public void recognizesMousePressReleasedBeforeRefresh() {
        key = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4);
        mapping.setKey(key);
        KeyMapping.resetMapping();
        pressAndRelease();

        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, false));
    }

    @Test
    public void recognizesAnotherTapWithoutAnObservedHeldState() {
        pressAndRelease();
        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));

        pressAndRelease();
        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
    }

    @Test
    public void recognizesReleaseAndRepressBetweenRefreshes() {
        mapping.setDown(true);
        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        mapping.setDown(false);
        pressAndRelease();

        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, true));
    }

    @Test
    public void drainsAccumulatedClicksIntoOnePendingPress() {
        pressAndRelease();
        pressAndRelease();
        pressAndRelease();

        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        Assertions.assertFalse(mapping.consumeClick());
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, false));
    }

    @Test
    public void preservesHeldStateWithoutRepeatingThePressEdge() {
        mapping.setDown(true);
        KeyMapping.click(key);

        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        Assertions.assertTrue(mapping.isDown());
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, true));
        Assertions.assertTrue(mapping.isDown());

        mapping.setDown(false);
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, true));
    }

    @Test
    public void recognizesHeldPressWithoutAQueuedClick() {
        mapping.setDown(true);

        Assertions.assertTrue(QuickCastFeature.consumeSpellPress(mapping, false));
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, true));
    }

    @Test
    public void discardedQueuedPressDoesNotReplay() {
        pressAndRelease();
        QuickCastFeature.consumeSpellPress(mapping, true);

        Assertions.assertFalse(mapping.consumeClick());
        Assertions.assertFalse(QuickCastFeature.consumeSpellPress(mapping, false));
    }

    private void pressAndRelease() {
        mapping.setDown(true);
        KeyMapping.click(key);
        mapping.setDown(false);
    }
}
