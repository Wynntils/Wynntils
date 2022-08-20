/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import java.util.LinkedList;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;

public class ScriptedContainerQuery {
    private final LinkedList<ScriptedQueryStep> steps;
    private final Consumer<String> errorHandler;

    protected ScriptedContainerQuery(LinkedList<ScriptedQueryStep> steps, Consumer<String> errorHandler) {
        this.steps = steps;
        this.errorHandler = errorHandler;
    }

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    public void executeQuery() {
        if (steps.isEmpty()) return;

        ScriptedQueryStep firstStep = steps.pop();
        ContainerQueryManager.runQuery(firstStep);
    }

    @FunctionalInterface
    public interface ContainerAction {
        void processContainer(ContainerContent container);
    }

    @FunctionalInterface
    public interface ContainerVerification {
        boolean verify(Component title, MenuType menuType);
    }

    public static class ScriptedQueryStep implements ContainerQueryStep {
        final ContainerAction startAction;
        final ContainerVerification verification;
        final ContainerAction handleContent;
        final LinkedList<ScriptedQueryStep> restOfTheSteps;

        public ScriptedQueryStep(
                ContainerAction startAction,
                ContainerVerification verification,
                ContainerAction handleContent,
                LinkedList<ScriptedQueryStep> restOfTheSteps) {
            this.startAction = startAction;
            this.verification = verification;
            this.handleContent = handleContent;
            this.restOfTheSteps = restOfTheSteps;
        }

        @Override
        public void startStep(ContainerContent container) {
            startAction.processContainer(container);
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
            if (restOfTheSteps.isEmpty()) return null;

            return restOfTheSteps.pop();
        }
    }

    public static class QueryBuilder {
        ContainerAction startAction;
        ContainerVerification verification;
        ContainerAction handleContent;

        LinkedList<ScriptedQueryStep> steps = new LinkedList<>();
        Consumer<String> errorHandler;

        public QueryBuilder onError(Consumer<String> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        private void checkForCompletion() {
            if (startAction != null && verification != null && handleContent != null) {
                ScriptedQueryStep nextStep = new ScriptedQueryStep(startAction, verification, handleContent, steps);
                steps.add(nextStep);
                startAction = null;
                verification = null;
                handleContent = null;
            }
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
                ContainerQueryManager.clickOnSlot(container.items(), clickedSlot);
            };
            checkForCompletion();
            return this;
        }

        public QueryBuilder useItemInHotbar(int slotNum) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                ContainerQueryManager.openInventory(slotNum);
            };
            checkForCompletion();
            return this;
        }

        public ScriptedContainerQuery build() {
            if (startAction != null || handleContent != null || verification != null) {
                throw new IllegalStateException("Partial contents only for last step");
            }
            return new ScriptedContainerQuery(steps, errorHandler);
        }
    }
}
