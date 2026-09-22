/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestScreenKeybindSuppression {
    private WynntilsKeyMapping mapping;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void createMapping() {
        mapping = new WynntilsKeyMapping(new KeyBindDefinition(
                "testScreenInput",
                "Test screen input",
                KeyBindManager.COMMANDS_CATEGORY,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_L,
                true));
    }

    @Test
    public void blocksKeysHeldBeforeScreenUntilReleaseAndFreshPress() {
        mapping.setDown(true);
        mapping.suppressScreenInput();
        Assertions.assertFalse(mapping.isDown());

        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(InputConstants.REPEAT, false);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());

        mapping.onInput(InputConstants.RELEASE, false);
        mapping.setDown(false);
        mapping.onInput(InputConstants.PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void inputConsumedByAScreenCannotActivateMapping() {
        mapping.onInput(InputConstants.PRESS, true);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(InputConstants.REPEAT, true);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void screenResetsDoNotClearSuppression() {
        mapping.suppressScreenInput();
        KeyMapping.releaseAll();
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void freshPressRecoversWhenReleaseWasNotObserved() {
        mapping.suppressScreenInput();
        mapping.onInput(InputConstants.PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void vanillaMappingsAreNotSuppressedByScreens() {
        KeyMapping vanillaMapping = new KeyMapping(
                "testVanillaInput", InputConstants.Type.KEYSYM, InputConstants.KEY_L, KeyBindManager.COMMANDS_CATEGORY);
        mapping.suppressScreenInput();
        vanillaMapping.setDown(true);
        mapping.setDown(true);
        Assertions.assertTrue(vanillaMapping.isDown());
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void mouseMappingsRemainUsableInAndAfterScreens() {
        mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4));
        mapping.suppressScreenInput();
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
        mapping.setDown(false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void rebindingDoesNotCarrySuppressionToAnotherKeyOrMouseButton() {
        mapping.suppressScreenInput();
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_K));
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());

        mapping.suppressScreenInput();
        mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4));
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void reapplyingTheSameBindingDoesNotLetAHeldScreenKeyThrough() {
        mapping.suppressScreenInput();
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_L));
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void scanCodeMappingsAlsoRespectSuppressionAndRelease() {
        mapping.setKey(InputConstants.Type.SCANCODE.getOrCreate(42));
        Assertions.assertTrue(mapping.matches(new KeyEvent(InputConstants.UNKNOWN.getValue(), 42, 0)));
        mapping.suppressScreenInput();
        mapping.onInput(InputConstants.REPEAT, false);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(InputConstants.RELEASE, false);
        mapping.onInput(InputConstants.PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void explicitScreenRestorationAllowsAHeldKeyToContinueAfterClosing() {
        mapping.suppressScreenInput();
        mapping.setDown(true, true);
        Assertions.assertTrue(mapping.isDown());
        mapping.setDown(true, false);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void forwardedRepeatsRemainUsableInScreens() {
        mapping.suppressScreenInput();
        mapping.setDown(true, true);
        mapping.onInput(InputConstants.REPEAT, true);
        Assertions.assertFalse(mapping.isDown());
        mapping.setDown(true, true);
        Assertions.assertTrue(mapping.isDown());
        mapping.setDown(true, false);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void openingAnotherScreenSuppressesPreviouslyForwardedInput() {
        mapping.setDown(true, true);
        mapping.suppressScreenInput();
        mapping.setDown(true, false);
        Assertions.assertFalse(mapping.isDown());
    }
}
