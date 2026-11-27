/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestKeyBindPresses {
    private WynntilsKeyMapping mapping;
    private KeyBind keyBind;
    private int presses;
    private InputConstants.Key key;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void createMapping() {
        key = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F8);
        mapping = new WynntilsKeyMapping(new KeyBindDefinition(
                "testKeyBindPress",
                "Test keybind press",
                KeyBindManager.COMBAT_CATEGORY,
                InputConstants.Type.KEYSYM,
                key.getValue(),
                true));
        KeyMapping.resetMapping();
        keyBind = createKeyBind(true, () -> presses++);
    }

    @AfterEach
    public void releaseMapping() {
        KeyMapping.releaseAll();
    }

    @Test
    public void recognizesKeyboardPressReleasedBeforeRefresh() {
        pressAndRelease();

        Assertions.assertFalse(mapping.isDown());
        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void recognizesMousePressReleasedBeforeRefresh() {
        key = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_4);
        mapping.setKey(key);
        KeyMapping.resetMapping();
        pressAndRelease();

        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void recognizesAnotherTapWithoutAnObservedHeldState() {
        pressAndRelease();
        Assertions.assertTrue(keyBind.consumePress());

        pressAndRelease();
        Assertions.assertTrue(keyBind.consumePress());
    }

    @Test
    public void recognizesReleaseAndRepressBetweenRefreshes() {
        mapping.setDown(true);
        Assertions.assertTrue(keyBind.consumePress());
        mapping.setDown(false);
        pressAndRelease();

        Assertions.assertTrue(keyBind.consumePress());
    }

    @Test
    public void coalescesAccumulatedTapsIntoOnePendingPress() {
        pressAndRelease();
        pressAndRelease();
        pressAndRelease();

        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void preservesHeldStateWithoutRepeatingThePressEdge() {
        mapping.setDown(true);
        KeyMapping.click(key);

        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertTrue(mapping.isDown());
        Assertions.assertFalse(keyBind.consumePress());
        Assertions.assertTrue(mapping.isDown());

        mapping.setDown(false);
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void recognizesHeldPressWithoutAQueuedClick() {
        mapping.setDown(true);

        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void discardedQueuedPressDoesNotReplay() {
        pressAndRelease();
        keyBind.resetPress();

        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void callbacksRecognizeShortTapsAndDoNotReplayThem() {
        pressAndRelease();
        keyBind.trigger();
        keyBind.trigger();
        Assertions.assertEquals(1, presses);

        pressAndRelease();
        keyBind.trigger();
        Assertions.assertEquals(2, presses);
    }

    @Test
    public void firstPressCallbacksDoNotRepeatWhileHeld() {
        mapping.setDown(true);
        KeyMapping.click(key);
        keyBind.trigger();
        mapping.setDown(true);
        KeyMapping.click(key);
        keyBind.trigger();
        Assertions.assertEquals(1, presses);
    }

    @Test
    public void repeatingCallbacksHandleTapsAndContinueWhileHeld() {
        keyBind = createKeyBind(false, () -> presses++);
        pressAndRelease();
        keyBind.trigger();
        keyBind.trigger();
        Assertions.assertEquals(1, presses);

        mapping.setDown(true);
        keyBind.trigger();
        keyBind.trigger();
        Assertions.assertEquals(3, presses);
        mapping.setDown(false);
        keyBind.trigger();
        Assertions.assertEquals(3, presses);
    }

    @Test
    public void managerDoesNotConsumePressesFromDirectlyPolledBindings() {
        keyBind = createKeyBind(true, null);
        pressAndRelease();
        keyBind.trigger();

        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void resetDiscardsQueuedCallbackPressesAndAllowsTheNextTap() {
        pressAndRelease();
        keyBind.resetPress();
        keyBind.trigger();
        Assertions.assertEquals(0, presses);

        pressAndRelease();
        keyBind.trigger();
        Assertions.assertEquals(1, presses);
    }

    @Test
    public void heldKeyRepeatDoesNotBecomeAFreshPressOnRelease() {
        mapping.setDown(true);
        keyBind.trigger();
        mapping.setDown(true);
        KeyMapping.click(key);
        mapping.setDown(false);
        keyBind.trigger();
        Assertions.assertEquals(1, presses);
    }

    @Test
    public void releaseAndRepressEndingHeldIsRecognized() {
        mapping.setDown(true);
        Assertions.assertTrue(keyBind.consumePress());
        mapping.setDown(false);
        mapping.setDown(true);
        Assertions.assertTrue(keyBind.consumePress());
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void focusLossAndRebindingDiscardPendingPresses() {
        pressAndRelease();
        KeyMapping.releaseAll();
        Assertions.assertFalse(keyBind.consumePress());

        pressAndRelease();
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F9));
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void screenSuppressionDiscardsPendingAndBlockedPresses() {
        pressAndRelease();
        mapping.suppressScreenInput();
        Assertions.assertFalse(keyBind.consumePress());
        mapping.setDown(true);
        mapping.setDown(false);
        Assertions.assertFalse(keyBind.consumePress());
    }

    @Test
    public void forwardedScreenRepeatsDoNotTriggerFirstPressAgain() {
        mapping.onInput(InputConstants.PRESS, true);
        mapping.setDown(true, true);
        keyBind.trigger();
        Assertions.assertEquals(1, presses);

        mapping.onInput(InputConstants.REPEAT, true);
        mapping.setDown(true, true);
        keyBind.trigger();
        Assertions.assertEquals(1, presses);
    }

    private KeyBind createKeyBind(boolean firstPress, Runnable onPress) {
        KeyBindDefinition definition = new KeyBindDefinition(
                "testKeyBindPress",
                "Test keybind press",
                KeyBindManager.COMBAT_CATEGORY,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_F8,
                firstPress);
        return new KeyBind(definition, mapping, onPress, null);
    }

    private void pressAndRelease() {
        mapping.setDown(true);
        KeyMapping.click(key);
        mapping.setDown(false);
    }
}
