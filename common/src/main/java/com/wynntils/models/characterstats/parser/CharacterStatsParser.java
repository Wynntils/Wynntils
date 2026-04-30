package com.wynntils.models.characterstats.parser;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.characterstats.type.PlayerStat;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterStatsParser {

    private static final int CHARACTER_STAT_SLOT = 7;

    private final Pattern PLAYERSTAT_IDENTIFICATION_PATTERN = Pattern.compile("§dIdentifications: ");
    private final Pattern PLAYERSTAT_ITEM_PATTERN = Pattern.compile("§7(?!§)(.*?)(?<statName>[\\w ]+): (?<positiveNegative>§a|§c)(?<value>[-+\\d]+)(?<unit>.*?$)");
    private final Pattern PLAYERSTAT_END_PATTERN = Pattern.compile("§5 ");
    private int pageCount = 0;
    private Consumer<PlayerStat> playerStatConsumer;

    public void queryPlayerStats(Consumer<PlayerStat> playerStatConsumer) {
        if (McUtils.player() == null) return;
        this.playerStatConsumer = playerStatConsumer;

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Player Stats Query")
                .onError(msg -> WynntilsMod.warn("Player stats querying: " + msg))

                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))

                .execute(() -> pageCount = 0)
                .reprocess(this::processPage)
                .repeat((c) -> {
                    pageCount++;
                    return pageCount < 4;
                }, QueryStep.clickOnSlot(CHARACTER_STAT_SLOT)
                        .verifyContentChange((a, b, c) -> true)
                        .processIncomingContainer(this::processPage))

                .build();

        query.executeQuery();
    }

    public void postponeQuery() {
        Handlers.ContainerQuery.endAllQueries();
    }

    private void processPage(ContainerContent content) {
        ItemStack stack = content.items().get(CHARACTER_STAT_SLOT);

        List<Component> lore = stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL);

        boolean reachedIdentifications = false;
        for (Component component : lore) {
            StyledText styledText = StyledText.fromComponent(component);
            if (styledText.matches(PLAYERSTAT_IDENTIFICATION_PATTERN)) {
                reachedIdentifications = true;
                continue;
            }

            if (!reachedIdentifications) continue;

            if (styledText.matches(PLAYERSTAT_END_PATTERN)) {
                break;
            }

            Matcher matcher = styledText.getMatcher(PLAYERSTAT_ITEM_PATTERN);
            if (matcher.find()) {
                String statName = matcher.group("statName");
                String positiveNegative = matcher.group("positiveNegative");
                int value = Integer.parseInt(matcher.group("value"));
                String unit = matcher.group("unit");

                StatType statType = Models.Stat.fromDisplayName(statName, unit);
                if (statType != null) {
                    PlayerStat playerStat = new PlayerStat(statType, value, positiveNegative.equals("§a"));
                    playerStatConsumer.accept(playerStat);
                } else {
                    WynntilsMod.warn("Could not find stat type for " + statName + " with unit " + unit);
                }
            }
        }
    }
}
