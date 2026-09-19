/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestAbilityTreeAnnotator {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @ParameterizedTest
    @CsvSource({
        "50, 50, 0, 50",
        "32, 32, 0, 32",
        "0, 45, 0, 45",
        "48, 45, 4, 49",
        "49, 45, 4, 49",
        "50, 45, 5, 50",
        "33, 32, 1, 33",
        "34, 32, 2, 34"
    })
    public void includesLoanedPointsInTotal(int available, int earned, int loaned, int expectedTotal) {
        for (String name : List.of("§b§lAbility Tree", "§#82eff4ff§lAbility Points", "§e§lAbility Points")) {
            List<Component> lore = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                lore.add(Component.empty());
            }
            lore.add(StyledText.fromString("§b✦ Available Points: §#a0c84bff" + available + "§7/" + earned)
                    .getComponent());
            if (loaned > 0) {
                lore.add(StyledText.fromString("§#a0c84bff" + loaned + " early " + (loaned == 1 ? "point" : "points")
                                + " from §eCHAMPION")
                        .getComponent());
            }
            while (lore.size() < 8) {
                lore.add(Component.empty());
            }
            lore.add(StyledText.fromString("§eShift Click to reset your tree").getComponent());
            ItemStack item = new ItemStack(Items.POTION);
            item.set(DataComponents.LORE, new ItemLore(lore));

            AbilityTreeItem annotation = Assertions.assertInstanceOf(
                    AbilityTreeItem.class, new AbilityTreeAnnotator().getAnnotation(item, StyledText.fromString(name)));
            Assertions.assertEquals(available, annotation.getCount());
            Assertions.assertEquals(expectedTotal, annotation.getTotalPoints());
            Assertions.assertEquals(name.equals("§e§lAbility Points"), annotation.getCanReset());
        }
    }
}
