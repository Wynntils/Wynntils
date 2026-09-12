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
import org.lwjgl.glfw.GLFW;

public class TestChatKeybindSuppression {
    private WynntilsKeyMapping mapping;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void createMapping() {
        mapping = new WynntilsKeyMapping(new KeyBindDefinition(
                "testChatInput",
                "Test chat input",
                KeyBindManager.COMMANDS_CATEGORY,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                true));
    }

    @Test
    public void blocksKeysHeldBeforeChatUntilReleaseAndFreshPress() {
        mapping.setDown(true);
        mapping.suppressChatInput();
        Assertions.assertFalse(mapping.isDown());

        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(GLFW.GLFW_REPEAT, false);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());

        mapping.onInput(GLFW.GLFW_RELEASE, false);
        mapping.setDown(false);
        mapping.onInput(GLFW.GLFW_PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void typingAndRepeatingInChatCannotActivateMapping() {
        mapping.onInput(GLFW.GLFW_PRESS, true);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(GLFW.GLFW_REPEAT, true);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void screenResetsDoNotClearChatSuppression() {
        mapping.suppressChatInput();
        KeyMapping.releaseAll();
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void freshPressRecoversWhenReleaseWasNotObserved() {
        mapping.suppressChatInput();
        mapping.onInput(GLFW.GLFW_PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void vanillaMappingsAreNotSuppressedByChat() {
        KeyMapping vanillaMapping = new KeyMapping(
                "testVanillaInput", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, KeyBindManager.COMMANDS_CATEGORY);
        mapping.suppressChatInput();
        vanillaMapping.setDown(true);
        mapping.setDown(true);
        Assertions.assertTrue(vanillaMapping.isDown());
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void mouseMappingsRemainUsableInAndAfterChat() {
        mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_4));
        mapping.suppressChatInput();
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
        mapping.setDown(false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void rebindingDoesNotCarrySuppressionToAnotherKeyOrMouseButton() {
        mapping.suppressChatInput();
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());

        mapping.suppressChatInput();
        mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_4));
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }

    @Test
    public void reapplyingTheSameBindingDoesNotLetAHeldChatKeyThrough() {
        mapping.suppressChatInput();
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_L));
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
    }

    @Test
    public void scanCodeMappingsAlsoRespectSuppressionAndRelease() {
        mapping.setKey(InputConstants.Type.SCANCODE.getOrCreate(42));
        Assertions.assertTrue(mapping.matches(new KeyEvent(GLFW.GLFW_KEY_UNKNOWN, 42, 0)));
        mapping.suppressChatInput();
        mapping.onInput(GLFW.GLFW_REPEAT, false);
        mapping.setDown(true);
        Assertions.assertFalse(mapping.isDown());
        mapping.onInput(GLFW.GLFW_RELEASE, false);
        mapping.onInput(GLFW.GLFW_PRESS, false);
        mapping.setDown(true);
        Assertions.assertTrue(mapping.isDown());
    }
}
