/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerContent;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestBookFeature extends UserFeature {
    private static final int NEXT_PAGE_SLOT = 8;
    private static final Pattern QUEST_NAME_MATCHER = Pattern.compile("^§.§l(.*)À $");
    private static final Pattern STATUS_MATCHER = Pattern.compile("^§.(.*)(?:\\.\\.\\.|!)$");
    private static final Pattern LENGTH_MATCHER = Pattern.compile("^§a-§r§7 Length: §r§f(.*)$");
    private static final Pattern LEVEL_MATCHER = Pattern.compile("^\"§a.§r§7 Combat Lv. Min: §r§f(\\d+)\"$");

    @RegisterKeyBind
    private final KeyBind questBookKeyBind =
            new KeyBind("Rescan Quest Book", GLFW.GLFW_KEY_UNKNOWN, true, this::queryQuestBook);

    private void processQuestBookPage(ContainerContent container, int page) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;
                // Very first slot is chat history
                if (slot == 0) continue;
                ItemStack item = container.items().get(slot);

                QuestInfo questInfo = getQuestInfo(item);
                if (questInfo == null) continue;
                System.out.println("%%%% GOT: " + questInfo);
            }
        }
    }

    private QuestInfo getQuestInfo(ItemStack item) {
        String name = getQuestName(item);
        if (name == null) return null;

        LinkedList<String> lore = ItemUtils.getLore(item);

        QuestStatus status = getQuestStatus(lore);
        if (status == null) return null;

        if (!skipEmptyLine(lore)) return null;

        String combatLevel = getRequirements(lore);

        QuestLength questLength = getQuestLength(lore);
        if (questLength == null) return null;

        if (!skipEmptyLine(lore)) return null;

        String description = getDescription(lore);

        QuestInfo questInfo = new QuestInfo(name, status, questLength, combatLevel, description);
        return questInfo;
    }

    private String getQuestName(ItemStack item) {
        String rawName = item.getHoverName().getString();
        if (rawName.trim().isEmpty()) {
            return null;
        }
        Matcher m = QUEST_NAME_MATCHER.matcher(rawName);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching quest name: " + rawName);
            return null;
        }
        String name = m.group(1);
        return name;
    }

    private QuestStatus getQuestStatus(LinkedList<String> lore) {
        String rawStatus = lore.pop();
        Matcher m3 = STATUS_MATCHER.matcher(rawStatus);
        if (!m3.find()) {
            WynntilsMod.warn("Non-matching status value: " + rawStatus);
            return null;
        }
        QuestStatus status = QuestStatus.fromString(m3.group(1));
        return status;
    }

    private boolean skipEmptyLine(LinkedList<String> lore) {
        String loreLine;

        loreLine = lore.pop();
        if (!loreLine.isEmpty()) {
            WynntilsMod.warn("Unexpected value in quest: " + loreLine);
            return false;
        }
        return true;
    }

    private String getRequirements(LinkedList<String> lore) {
        String loreLine;
        String combatLevel = "";
        loreLine = lore.getFirst();
        while (loreLine.contains("Lv. Min")) {
            lore.pop();
            if (loreLine.contains("Combat Lv. Min")) {
                System.out.println("got  level req:" + loreLine);
                combatLevel = loreLine;
                // §a✔§r§7 Combat Lv. Min: §r§f4
                // §c✖§r§7 Combat Lv. Min: §r§f54
            } else {
                System.out.println("####### GOT OTHER REQ:" + loreLine);
                // §a✔§r§7 Fishing Lv. Min: §r§f1
                // §c✖§r§7 Mining Lv. Min: §r§f15
                // §c✖§r§7 Farming Lv. Min: §r§f20

                // ####### GOT OTHER REQ:§c✖§r§7 Mining Lv. Min: §r§f20
                // ####### GOT OTHER REQ:§c✖§r§7 Woodcutting Lv. Min: §r§f20
                // ####### GOT OTHER REQ:§c✖§r§7 Fishing Lv. Min: §r§f20
                // Note: one quest can have multiple!!!
            }
            loreLine = lore.getFirst();
        }
        return combatLevel;
    }

    private QuestLength getQuestLength(LinkedList<String> lore) {
        String length = lore.pop();

        Matcher m2 = LENGTH_MATCHER.matcher(length);
        if (!m2.find()) {
            WynntilsMod.warn("Non-matching quest length: " + length);
            return null;
        }
        QuestLength questLength = QuestLength.fromString(m2.group(1));
        return questLength;
    }

    private String getDescription(LinkedList<String> lore) {
        List<String> descriptionLines = lore.subList(0, lore.size() - 2);
        String description = String.join(
                        " ",
                        descriptionLines.stream().map(line -> line.substring(2)).toList())
                .replaceAll("  ", " ")
                .trim();
        return description;
    }

    private String getNextPageButtonName(int nextPageNum) {
        return "[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]";
    }

    private String getQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Quests$";
    }

    private void queryQuestBook() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Quest Book Query")
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer(c -> processQuestBookPage(c, 1))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(2))
                .matchTitle(getQuestBookTitle(2))
                .processContainer(c -> processQuestBookPage(c, 2))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(3))
                .matchTitle(getQuestBookTitle(3))
                .processContainer(c -> processQuestBookPage(c, 3))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(4))
                .matchTitle(getQuestBookTitle(4))
                .processContainer(c -> processQuestBookPage(c, 4))
                .onError(msg -> WynntilsMod.warn("Error querying Quest Book:" + msg))
                .build();

        query.executeQuery();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            WynntilsMod.info("Scheduling quest book query");
            queryQuestBook();
        }
    }

    public class QuestInfo {
        private final String name;
        private final QuestStatus status;
        private final QuestLength length;
        private final String minLevel;
        private final String nextTask;

        public QuestInfo(String name, QuestStatus status, QuestLength length, String minLevel, String nextTask) {
            this.name = name;
            this.status = status;
            this.length = length;
            this.minLevel = minLevel;
            this.nextTask = nextTask;
        }

        @Override
        public String toString() {
            return "QuestInfo[" + "name=\""
                    + name + "\", " + "status="
                    + status + ", " + "length="
                    + length + ", " + "minLevel="
                    + minLevel + ", " + "nextTask=\""
                    + nextTask + "\"]";
        }
    }

    public enum QuestStatus {
        COMPLETED,
        STARTED,
        CAN_START,
        CANNOT_START;

        public static QuestStatus fromString(String str) {
            try {
                return QuestStatus.valueOf(str.toUpperCase(Locale.ROOT).replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                // Use CANNOT_START as fallback... it's as good as any
                return CANNOT_START;
            }
        }
    }

    public enum QuestLength {
        SHORT,
        MEDIUM,
        LONG;

        public static QuestLength fromString(String str) {
            try {
                return QuestLength.valueOf(str.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // Use SHORT as fallback... it's as good as any
                return SHORT;
            }
        }
    }
}
