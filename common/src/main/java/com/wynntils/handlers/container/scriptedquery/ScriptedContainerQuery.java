/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.ContainerQueryStep;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.LinkedList;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ScriptedContainerQuery implements ContainerQueryStep {
    private final LinkedList<QueryStep> steps;
    private final Consumer<String> errorHandler;
    private final String name;
    private QueryStep currentStep = null;

    ScriptedContainerQuery(String name, LinkedList<QueryStep> steps, Consumer<String> errorHandler) {
        this.name = name;
        this.steps = steps;
        this.errorHandler = errorHandler;
    }

    public static QueryBuilder builder(String name) {
        return new QueryBuilder(name);
    }

    public static boolean containerHasSlot(
            ContainerContent container, int slotNum, Item expectedItemType, StyledText expectedItemName) {
        ItemStack itemStack = container.items().get(slotNum);
        return itemStack.is(expectedItemType)
                && ItemUtils.getItemName(itemStack).equals(expectedItemName);
    }

    public void executeQuery() {
        if (!popOneStep()) return;

        Handlers.ContainerQuery.runQuery(this);
    }

    @Override
    public boolean startStep(ContainerContent container) throws ContainerQueryException {
        return currentStep.startStep(this, container);
    }

    @Override
    public boolean verifyContainer(Component title, MenuType<?> menuType) {
        return currentStep.getVerification().verify(title, menuType);
    }

    @Override
    public void handleContent(ContainerContent container) throws ContainerQueryException {
        currentStep.getHandleContent().processContainer(container);
    }

    @Override
    public ContainerQueryStep getNextStep(ContainerContent container) {
        return currentStep.getNextStep(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onError(String errorMsg) {
        errorHandler.accept(errorMsg);
        // Remove all remaining steps
        currentStep = null;
        steps.clear();
    }

    boolean popOneStep() {
        if (steps.isEmpty()) {
            currentStep = null;
            return false;
        }

        this.currentStep = steps.pop();
        return true;
    }
}
