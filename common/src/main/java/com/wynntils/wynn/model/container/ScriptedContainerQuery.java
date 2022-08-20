/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.LinkedList;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ScriptedContainerQuery {
    private static final Consumer<String> DEFAULT_ERROR_HANDLER =
            (errorMsg) -> WynntilsMod.warn("Error in ScriptedContainerQuery");
    private final LinkedList<ScriptedQueryStep> steps = new LinkedList<>();
    private Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;

    public static QueryBuilder builder() {
        return new QueryBuilder(new ScriptedContainerQuery());
    }

    public void executeQuery() {
        if (steps.isEmpty()) return;

        ScriptedQueryStep firstStep = steps.pop();
        ContainerQueryManager.runQuery(firstStep);
    }

    private void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @FunctionalInterface
    private interface StartAction {
        boolean execute(ContainerContent container);
    }

    @FunctionalInterface
    private interface ContainerVerification {
        boolean verify(Component title, MenuType menuType);
    }

    @FunctionalInterface
    public interface ContainerAction {
        void processContainer(ContainerContent container);
    }

    private class ScriptedQueryStep implements ContainerQueryStep {
        final StartAction startAction;
        final ContainerVerification verification;
        final ContainerAction handleContent;

        private ScriptedQueryStep(
                StartAction startAction, ContainerVerification verification, ContainerAction handleContent) {
            this.startAction = startAction;
            this.verification = verification;
            this.handleContent = handleContent;
        }

        @Override
        public boolean startStep(ContainerContent container) {
            return startAction.execute(container);
        }

        @Override
        public boolean verifyContainer(Component title, MenuType menuType) {
            return verification.verify(title, menuType);
        }

        @Override
        public void handleContent(ContainerContent container) {
            handleContent.processContainer(container);
        }

        @Override
        public ContainerQueryStep getNextStep(ContainerContent container) {
            if (ScriptedContainerQuery.this.steps.isEmpty()) return null;

            return ScriptedContainerQuery.this.steps.pop();
        }

        @Override
        public void onError(String errorMsg) {
            ScriptedContainerQuery.this.errorHandler.accept(errorMsg);
            // Remove all remaining steps
            while (!ScriptedContainerQuery.this.steps.isEmpty()) {
                ScriptedContainerQuery.this.steps.removeFirst();
            }
        }
    }

    public static class QueryBuilder {
        StartAction startAction;
        ContainerVerification verification;
        ContainerAction handleContent;

        ScriptedContainerQuery query;

        private QueryBuilder(ScriptedContainerQuery scriptedContainerQuery) {
            query = scriptedContainerQuery;
        }

        public QueryBuilder onError(Consumer<String> errorHandler) {
            query.setErrorHandler(errorHandler);
            return this;
        }

        public QueryBuilder expectTitle(String expectedTitle) {
            if (verification != null) {
                throw new IllegalStateException("Set verification twice");
            }
            this.verification = (title, type) -> title.getString().equals(expectedTitle);
            checkForCompletion();
            return this;
        }

        public QueryBuilder processContainer(ContainerAction action) {
            if (handleContent != null) {
                throw new IllegalStateException("Set handleContent twice");
            }
            this.handleContent = action;
            checkForCompletion();
            return this;
        }

        public QueryBuilder clickOnSlot(int clickedSlot) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                ContainerUtils.clickOnSlot(clickedSlot, container.containerId(), container.items());
                return true;
            };
            checkForCompletion();
            return this;
        }

        public QueryBuilder clickOnSlotMatching(int clickedSlot, Item itemType, String name) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                ItemStack item = container.items().get(clickedSlot);
                if (!item.is(itemType) || !item.getDisplayName().getString().equals(name)) return false;

                ContainerUtils.clickOnSlot(clickedSlot, container.containerId(), container.items());
                return true;
            };
            checkForCompletion();
            return this;
        }

        public QueryBuilder useItemInHotbar(int slotNum) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> ContainerUtils.openInventory(slotNum);
            checkForCompletion();
            return this;
        }

        public ScriptedContainerQuery build() {
            if (startAction != null || handleContent != null || verification != null) {
                throw new IllegalStateException("Partial contents only for last step");
            }
            return query;
        }

        private void checkForCompletion() {
            if (startAction != null && verification != null && handleContent != null) {
                ScriptedQueryStep nextStep = query.new ScriptedQueryStep(startAction, verification, handleContent);
                query.steps.add(nextStep);
                startAction = null;
                verification = null;
                handleContent = null;
            }
        }
    }
}
