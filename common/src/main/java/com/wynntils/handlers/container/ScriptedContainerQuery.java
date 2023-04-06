/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.CodedString;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.LinkedList;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ScriptedContainerQuery {
    private static final Consumer<String> DEFAULT_ERROR_HANDLER =
            (errorMsg) -> WynntilsMod.warn("Error in ScriptedContainerQuery");

    // No op
    private static final Runnable DEFAULT_ON_COMPLETE = () -> {};
    private final LinkedList<ScriptedQueryStep> steps = new LinkedList<>();
    private Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;
    private Runnable onComplete = DEFAULT_ON_COMPLETE;
    private final String name;

    private ScriptedContainerQuery(String name) {
        this.name = name;
    }

    public static QueryBuilder builder(String name) {
        return new QueryBuilder(new ScriptedContainerQuery(name));
    }

    public void executeQuery() {
        if (steps.isEmpty()) return;

        ScriptedQueryStep firstStep = steps.pop();
        Handlers.ContainerQuery.runQuery(firstStep);
    }

    private void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @FunctionalInterface
    private interface StartAction {
        boolean execute(ContainerContent container);
    }

    @FunctionalInterface
    private interface ContainerVerification {
        boolean verify(Component title, MenuType<?> menuType);
    }

    @FunctionalInterface
    public interface ContainerAction {
        void processContainer(ContainerContent container);
    }

    private final class ScriptedQueryStep implements ContainerQueryStep {
        final StartAction startAction;
        final ContainerVerification verification;
        final ContainerAction handleContent;
        final boolean waitForMenuReopen;

        private ScriptedQueryStep(
                StartAction startAction,
                ContainerVerification verification,
                ContainerAction handleContent,
                boolean waitForMenuReopen) {
            this.startAction = startAction;
            this.verification = verification;
            this.handleContent = handleContent;
            this.waitForMenuReopen = waitForMenuReopen;
        }

        @Override
        public boolean startStep(ContainerContent container) {
            return startAction.execute(container);
        }

        @Override
        public boolean verifyContainer(Component title, MenuType<?> menuType) {
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
            ScriptedContainerQuery.this.steps.clear();
        }

        @Override
        public void onComplete() {
            ScriptedContainerQuery.this.onComplete.run();
        }

        @Override
        public String getName() {
            return ScriptedContainerQuery.this.name;
        }

        @Override
        public boolean shouldWaitForMenuReopen() {
            return this.waitForMenuReopen;
        }
    }

    /**
     * The QueryBuilder builds a ScriptedContainerQuery, which is a sequence of ContainerQueryStep,
     * which are executed one after another. Each step requires three parts:
     * 1) a startAction (to open the container)
     * 2) a verification (to check that we got the right container)
     * 3) a handleContent (to actually consume the content of the container)
     *
     * The builder will accept these three in any order, and create a ContainerQueryStep for each
     * such triplet. It will not allow the creation of a step where one of them are missing.
     */
    public static final class QueryBuilder {
        private StartAction startAction;
        private ContainerVerification verification;
        private ContainerAction handleContent;
        private boolean waitForMenuReopen = true;

        private final ScriptedContainerQuery query;

        private QueryBuilder(ScriptedContainerQuery scriptedContainerQuery) {
            query = scriptedContainerQuery;
        }

        public QueryBuilder setWaitForMenuReopen(boolean wait) {
            this.waitForMenuReopen = wait;
            return this;
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

        public QueryBuilder matchTitle(String regExp) {
            if (verification != null) {
                throw new IllegalStateException("Set verification twice");
            }
            this.verification = (title, type) -> title.getString().matches(regExp);
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

        public QueryBuilder clickOnSlot(int slotNum) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                ContainerUtils.clickOnSlot(
                        slotNum, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
                return true;
            };
            checkForCompletion();
            return this;
        }

        public QueryBuilder clickOnSlotWithName(int slotNum, Item expectedItemType, CodedString expectedItemName) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                ItemStack itemStack = container.items().get(slotNum);
                if (!itemStack.is(expectedItemType)
                        || !CodedString.fromComponentIgnoringComponentStylesAndJustUsingFormattingCodes(
                                        itemStack.getDisplayName())
                                .equals(expectedItemName)) return false;

                ContainerUtils.clickOnSlot(
                        slotNum, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
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
                ScriptedQueryStep nextStep =
                        query.new ScriptedQueryStep(startAction, verification, handleContent, waitForMenuReopen);
                query.steps.add(nextStep);
                startAction = null;
                verification = null;
                handleContent = null;
            }
        }
    }
}
