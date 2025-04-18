/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.containers.containers.CharacterSelectionContainer;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class CharacterSelectionModel extends Model {
    private static final List<Integer> CHARACTER_SLOTS = List.of(9, 10, 11, 18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46);
    private static final StyledText CREATE_CHARACTER_NAME = StyledText.fromString("§a§lCreate a Character");
    private List<Integer> validCharacterSlots = new ArrayList<>();
    private List<ItemStack> selectionScreenItems = new ArrayList<>();

    public CharacterSelectionModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onContainerItemsSet(ContainerSetContentEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof CharacterSelectionContainer)) return;

        validCharacterSlots = new ArrayList<>();
        selectionScreenItems = event.getItems();

        for (int currentSlot : CHARACTER_SLOTS) {
            StyledText itemName = StyledText.fromComponent(
                    selectionScreenItems.get(currentSlot).getHoverName());

            if (itemName.equals(CREATE_CHARACTER_NAME)) break;

            validCharacterSlots.add(currentSlot);
        }
    }

    public void playWithCharacter(int slot) {
        if (!(Models.Container.getCurrentContainer() instanceof CharacterSelectionContainer characterContainer)) return;

        ContainerUtils.clickOnSlot(
                slot, characterContainer.getContainerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, selectionScreenItems);
    }

    public List<Integer> getValidCharacterSlots() {
        return Collections.unmodifiableList(validCharacterSlots);
    }
}
