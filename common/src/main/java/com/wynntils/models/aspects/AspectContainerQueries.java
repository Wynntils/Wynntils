package com.wynntils.models.aspects;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AspectContainerQueries {
    private static final int ABILITY_TREE_SLOT = 9;
    private static final int ASPECTS_BUTTON_SLOT = 86;

    public void dumpAspectContainer(
            Consumer<SavableAspectSet> supplier,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        queryAspectContainer(
                new AspectContainerQueries.AspectContainerDumper(supplier),
                onStatus, onError, onComplete);
    }

    public void applyAspectLoadout(
            List<String> aspectsToEquip,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        // TODO: Implement once slot diffing logic is finalized
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void queryAspectContainer(
            AspectContainerProcessor processor,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {

        QueryBuilder builder = ScriptedContainerQuery.builder("Aspect Container Dumper")
                .onError(msg -> {
                    onError.accept(msg);
                    WynntilsMod.error(msg);
                })
                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))
                .execute(() -> onStatus.accept("Compass menu"))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))
                .execute(() -> onStatus.accept("Ability tree menu"))

                // Open aspects menu
                .then(QueryStep.clickOnSlot(ASPECTS_BUTTON_SLOT).expectContainer(AbilityTreeContainer.class, AspectsContainer.class)
                        .verifyContentChange((container, changes, changeType) ->
                                Models.Container.getCurrentContainer() instanceof AspectsContainer)
                        .processIncomingContainer(processor::processContainer))
                .execute(() -> onStatus.accept("Aspects menu"));


        builder.execute(() -> onComplete.accept("Finished dumping aspects"));
        builder.build().executeQuery();
    }

    private abstract static class AspectContainerProcessor {
        protected abstract void processContainer(ContainerContent content);
    }

    /**
     * Reads the equipped aspect slots from the container and emits a SavableAspectLoadout.
     */
    private static class AspectContainerDumper extends AspectContainerProcessor {
        private final Consumer<SavableAspectSet> supplier;

        protected AspectContainerDumper(Consumer<SavableAspectSet> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected void processContainer(ContainerContent content) {
            List<String> currentAspects = new ArrayList<>();
            ClassType classType = ClassType.NONE;

            if (Models.Container.getCurrentContainer() instanceof AspectsContainer aspectsContainer) {
                for (int slot : aspectsContainer.getEquippedSlots()) {
                    if (slot >= content.items().size()) continue;

                    ItemStack stack = content.items().get(slot);
                    if (stack.isEmpty()) continue;

                    Optional<AspectItem> aspectOpt = Models.Item.asWynnItem(stack, AspectItem.class);
                    if (aspectOpt.isEmpty()) continue;

                    currentAspects.add(aspectOpt.get().getName());

                    // Derive class type from the first valid aspect if not yet known
                    if (classType == ClassType.NONE) {
                        AspectInfo info = aspectOpt.get().getAspectInfo();
                        if (info != null && info.classType() != null) {
                            classType = info.classType();
                        }
                    }
                }
            }

            // Fallback to the active character class if we couldn't determine it from aspects
            if (classType == ClassType.NONE) {
                classType = Models.Character.getClassType();
            }

            supplier.accept(new SavableAspectSet(currentAspects, classType));
        }
    }
}