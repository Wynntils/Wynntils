/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.character.actionbar.matchers.CharacterCreationSegmentMatcher;
import com.wynntils.models.character.actionbar.matchers.CharacterSelectionClassSegmentMatcher;
import com.wynntils.models.character.actionbar.matchers.CharacterSelectionLevelSegmentMatcher;
import com.wynntils.models.character.actionbar.matchers.CharacterSelectionSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterCreationSegment;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionClassSegment;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionLevelSegment;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionSegment;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.containers.CharacterSelectionContainer;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class CharacterSelectionModel extends Model {
    private static final List<Integer> CHARACTER_SLOTS =
            List.of(9, 10, 11, 18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46, 47);
    private static final StyledText CREATE_CHARACTER_NAME = StyledText.fromString("§a§lCreate a Character");
    private List<Integer> validCharacterSlots = new ArrayList<>();
    private List<ItemStack> selectionScreenItems = new ArrayList<>();
    private ClassType currentCharacterClass = ClassType.NONE;
    private boolean isReskinned = false;
    private int currentCharacterLevel = 1;
    private boolean isCreatingCharacter = false;

    public CharacterSelectionModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new CharacterCreationSegmentMatcher());
        Handlers.ActionBar.registerSegment(new CharacterSelectionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new CharacterSelectionClassSegmentMatcher());
        Handlers.ActionBar.registerSegment(new CharacterSelectionLevelSegmentMatcher());
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

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(CharacterSelectionClassSegment.class, this::updateCurrentCharacterClass);
        event.runIfPresent(CharacterSelectionLevelSegment.class, this::updateCurrentCharacterLevel);
        event.runIfPresent(CharacterCreationSegment.class, this::setCreatingCharacter);
        event.runIfPresent(CharacterSelectionSegment.class, this::setSelectingCharacter);
    }

    @SubscribeEvent
    public void onArmSwing(ArmSwingEvent e) {
        if (isCreatingCharacter) return;
        if (McUtils.screen() != null) return;
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        if (Models.WorldState.getCurrentState() != WorldState.CHARACTER_SELECTION) return;

        Models.Character.setSelectedCharacterFromCharacterSelection(
                currentCharacterClass, isReskinned, currentCharacterLevel);
    }

    public void playWithCharacter(int slot) {
        if (!(Models.Container.getCurrentContainer() instanceof CharacterSelectionContainer characterContainer)) return;

        // ContainerClickEvent will get the air item and not parse the character properly so pass it the correct item
        Models.Character.handleSelectedCharacter(selectionScreenItems.get(slot));
        ContainerUtils.clickOnSlot(
                slot, characterContainer.getContainerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, selectionScreenItems);
    }

    public List<Integer> getValidCharacterSlots() {
        return Collections.unmodifiableList(validCharacterSlots);
    }

    private void updateCurrentCharacterClass(CharacterSelectionClassSegment characterSelectionClassSegment) {
        currentCharacterClass = characterSelectionClassSegment.getClassType();
        isReskinned = characterSelectionClassSegment.isReskinned();
    }

    private void updateCurrentCharacterLevel(CharacterSelectionLevelSegment characterSelectionLevelSegment) {
        currentCharacterLevel = characterSelectionLevelSegment.getLevel();
    }

    private void setCreatingCharacter(CharacterCreationSegment characterCreationSegment) {
        isCreatingCharacter = true;
    }

    private void setSelectingCharacter(CharacterSelectionSegment characterSelectionSegment) {
        isCreatingCharacter = false;
    }
}
