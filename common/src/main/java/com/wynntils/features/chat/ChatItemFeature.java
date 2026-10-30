/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.mc.event.ChatComponentClickedEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.mixin.accessors.ChatScreenAccessor;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.screens.itemsharing.ItemSharingScreen;
import com.wynntils.screens.itemsharing.SavedItemsScreen;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class ChatItemFeature extends Feature {
    private static final Identifier CHANGE_ITEM_PAGE =
            Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "change_item_page");

    @RegisterKeyBind
    private final KeyBind shareItemKeybind = KeyBindDefinition.SHARE_ITEM.create(slot -> shareItem(slot, true));

    @RegisterKeyBind
    private final KeyBind saveItemKeybind =
            KeyBindDefinition.SAVE_ITEM_TO_RECORD.create(slot -> shareItem(slot, false));

    @RegisterKeyBind
    private final KeyBind itemRecordKeybind =
            KeyBindDefinition.OPEN_ITEM_RECORD.create(() -> McUtils.setScreen(SavedItemsScreen.create()));

    @Persisted
    private final Config<Boolean> showSharingScreen = new Config<>(true);

    @Persisted
    private final Config<Boolean> showPerfectOrDefective = new Config<>(true);

    private final Map<String, String> chatItems = new HashMap<>();

    private PageParser pageParser;

    public ChatItemFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onKeyTyped(KeyInputEvent e) {
        if (!Models.WorldState.onWorld()) return;
        if (!(McUtils.screen() instanceof ChatScreen chatScreen)) return;

        EditBox chatInput = ((ChatScreenAccessor) chatScreen).getChatInput();

        if (!chatItems.isEmpty()
                && (e.getKey() == InputConstants.KEY_RETURN || e.getKey() == InputConstants.KEY_NUMPADENTER)) {
            // replace the placeholder strings with the actual encoded strings
            for (Map.Entry<String, String> item : chatItems.entrySet()) {
                chatInput.setValue(chatInput.getValue().replace("<" + item.getKey() + ">", item.getValue()));
            }
            chatItems.clear();
            return;
        }

        // replace encoded strings with placeholders for less confusion

        // check for new chat item encoding
        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern().matcher(chatInput.getValue());
        while (matcher.find()) {
            String itemName = matcher.group("name");
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group("data"));
            ErrorOr<WynnItem> errorOrDecodedItem = Models.ItemEncoding.decodeItem(encodedByteBuffer, itemName);

            String name = getItemName(errorOrDecodedItem);

            // Only hide the encoded data behind the placeholder. A crafted item's quoted name must
            // remain ordinary chat text so Wynncraft moderation can inspect it.
            chatInput.setValue(chatInput.getValue().replace(matcher.group(), "<" + name + ">"));
            chatItems.put(name, matcher.group());
        }
    }

    private String getItemName(ErrorOr<WynnItem> errorOrDecodedItem) {
        String name;
        if (errorOrDecodedItem.hasError()) {
            name = "encoding_error";
        } else {
            WynnItem decodedItem = errorOrDecodedItem.getValue();

            name = decodedItem.getClass().getSimpleName();

            if (decodedItem instanceof NamedItemProperty namedItemProperty) {
                name = namedItemProperty.getName();

                if (decodedItem instanceof MountItem mountItem) {
                    name = StringUtils.toPossessive(mountItem.getName())
                            + " "
                            + (mountItem.isSummonItem()
                                    ? mountItem.getMountType().getSummonItemName()
                                    : mountItem.getMountType().getMountItemName());
                }
            }
        }

        while (chatItems.containsKey(name)) { // avoid overwriting entries
            name += "_";
        }
        return name;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatMessageEvent.Edit e) {
        StyledText message = e.getMessage();

        StyledText unwrapped = StyledTextUtils.unwrap(message);

        StyledText modified = decodeChatEncoding(unwrapped);

        if (modified.equals(unwrapped)) return;

        // If the message had any chat items, we use our unwrapped version
        // FIXME: This edited message could be re-wrapped if it is too long, to match the original message's style
        e.setMessage(modified);
    }

    private void shareItem(Slot hoveredSlot, boolean share) {
        if (hoveredSlot == null || pageParser != null) return;

        // Special case for unidentified items
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), GearItem.class);
        Optional<TomeItem> tomeItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), TomeItem.class);
        Optional<CharmItem> charmItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), CharmItem.class);
        if ((gearItemOpt.isPresent() && gearItemOpt.get().isUnidentified())
                || (tomeItemOpt.isPresent() && tomeItemOpt.get().isUnidentified())
                || (charmItemOpt.isPresent() && charmItemOpt.get().isUnidentified())) {
            // We can only send chat encoded items of identified items
            WynntilsMod.warn("Cannot make chat link of unidentified items");
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.chatItem.chatItemUnidentifiedError"));
            return;
        }

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(hoveredSlot.getItem());
        if (wynnItemOpt.isEmpty()) return;

        WynnItem item = wynnItemOpt.get();

        // Don't try to encode unsupported items
        if (!Models.ItemEncoding.canEncodeItem(item)) return;

        if (item instanceof PagedItemProperty pagedItem
                && !(hoveredSlot.getItem() instanceof FakeItemStack)
                && McUtils.screen() instanceof AbstractContainerScreen<?> screen
                && screen.getMenu() == McUtils.containerMenu()) {
            ItemSharingScreen sharingScreen = share && showSharingScreen.get()
                    ? ItemSharingScreen.createParsing(item, hoveredSlot.getItem(), (pagedItem.currentPage() + 1) % 3)
                    : null;
            pageParser = new PageParser(screen, screen.getMenu(), hoveredSlot, pagedItem, share, sharingScreen);

            if (sharingScreen != null) {
                McUtils.setScreen(sharingScreen);
            }

            requestNextPage();
            return;
        }

        finishSharing(hoveredSlot, item, share);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (pageParser == null) return;

        PageParser pendingParser = pageParser;
        if (!Models.WorldState.onWorld()
                || McUtils.containerMenu() != pendingParser.menu()
                || Models.Item.getWynnItem(pendingParser.slot().getItem()).orElse(null) != pendingParser.item()) {
            pendingParser.state().cancel();

            if (pendingParser.sharingScreen() != null) {
                pendingParser.sharingScreen().parsingFailed();
            }

            pageParser = null;
            return;
        }

        if (pendingParser.state().isWaitingToSend()) {
            if (pendingParser.state().canSend()) {
                requestNextPage();
            }
            return;
        }

        if (pendingParser.item().currentPage() != pendingParser.expectedPage()) {
            if (!pendingParser.timedOut() && pendingParser.state().shouldRetry()) {
                requestNextPage();
                return;
            }

            pendingParser.setWaitTicks(pendingParser.waitTicks() + 1);
            if (pendingParser.waitTicks() == 100) {
                pendingParser.setTimedOut(true);
                pendingParser.state().cancel();

                if (pendingParser.sharingScreen() != null) {
                    pendingParser.sharingScreen().parsingFailed();
                }

                McUtils.sendErrorToClient(I18n.get("feature.wynntils.chatItem.pageCollectionTimeout"));
            }

            return;
        }

        pendingParser.setRemainingSwaps(pendingParser.remainingSwaps() - 1);
        if (pendingParser.remainingSwaps() > 0) {
            pendingParser.state().acknowledged();
            return;
        }

        pendingParser.state().cancel();
        pageParser = null;

        // We know all 3 swaps have been acknowledged, don't reopen a screen that the user has left
        if (!pendingParser.timedOut() && pendingParser.isScreenCurrent()) {
            if (pendingParser.sharingScreen() != null) {
                pendingParser
                        .sharingScreen()
                        .completeParsing(pendingParser.slot().getItem());
            } else {
                finishSharing(pendingParser.slot(), (WynnItem) pendingParser.item(), pendingParser.share());
            }
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent event) {
        // Don't allow the item to be clicked until parsing has finished
        if (pageParser != null
                && event.getContainerMenu() == pageParser.menu()
                && event.getSlotNum() == pageParser.slot().index) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPageCycleSlot(SetSlotEvent.Post event) {
        if (pageParser == null
                || event.getContainer() != pageParser.slot().container
                || event.getSlot() != pageParser.slot().getContainerSlot()) return;

        Optional<PagedItemProperty> pagedItem =
                Models.Item.asWynnItemProperty(event.getItemStack(), PagedItemProperty.class);
        if (pagedItem.isEmpty()) {
            WynntilsMod.error("Expecting page change but item is not a paged item");

            if (pageParser.sharingScreen() != null) {
                pageParser.sharingScreen().parsingFailed();
            }

            pageParser.state().cancel();
            pageParser = null;
            return;
        }

        if (Models.Item.getWynnItem(event.getItemStack()).orElse(null) == pageParser.item()
                && pagedItem.get().currentPage() == (pageParser.expectedPage() + 2) % 3) {
            pageParser.state().unchangedResponse();
        }
    }

    @SubscribeEvent
    public void onChatItemClicked(ChatComponentClickedEvent event) {
        if (event.allowsInsertions()) return;

        Style clicked = event.getClicked();
        if (!(clicked.getClickEvent() instanceof ClickEvent.Custom click)
                || !click.id().equals(CHANGE_ITEM_PAGE)) return;

        if (clicked.getHoverEvent() instanceof HoverEvent.ShowItem(ItemStack item)
                && item instanceof FakeItemStack itemStack) {
            itemStack.cyclePage();
            event.setCanceled(true);
        }
    }

    private StyledText decodeChatEncoding(StyledText message) {
        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern().matcher(message.getStringWithoutFormatting());
        List<StyledText> decodedParts = new ArrayList<>();
        int previousEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > previousEnd) {
                decodedParts.add(message.substring(previousEnd, matcher.start(), StyleType.NONE));
            }

            String itemName = matcher.group("name");
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group("data"));
            ErrorOr<WynnItem> errorOrDecodedItem =
                    Models.ItemEncoding.decodeItemWithTrustedName(encodedByteBuffer, itemName);

            decodedParts.add(
                    errorOrDecodedItem.hasError()
                            ? StyledText.fromPart(createErrorPart(matcher.group(), errorOrDecodedItem.getError()))
                            : StyledText.fromParts(createItemPart(errorOrDecodedItem.getValue())));
            previousEnd = matcher.end();
        }

        if (decodedParts.isEmpty()) return message;

        if (previousEnd < message.length(StyleType.NONE)) {
            decodedParts.add(message.substring(previousEnd, StyleType.NONE));
        }

        return StyledText.concat(decodedParts);
    }

    private StyledTextPart createErrorPart(String originalString, String error) {
        // Display the original string with a red underline and a tooltip with the error message
        Style style = Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE).withColor(ChatFormatting.RED);

        style = style.withHoverEvent(
                new HoverEvent.ShowText(Component.literal(error).withStyle(ChatFormatting.RED)));

        return new StyledTextPart(originalString, style, null, Style.EMPTY);
    }

    private List<StyledTextPart> createItemPart(WynnItem wynnItem) {
        List<StyledTextPart> parts = new ArrayList<>();

        StyledText nameText = StyledText.fromString(wynnItem.getClass().getSimpleName());

        if (wynnItem instanceof NamedItemProperty namedItemProperty) {
            nameText = StyledText.fromString(namedItemProperty.getName());

            if (wynnItem instanceof ShinyItemProperty shinyItemProperty
                    && shinyItemProperty.getShinyStat().isPresent()) {
                parts.add(new StyledTextPart("⬡ ", Style.EMPTY.withColor(ChatFormatting.WHITE), null, Style.EMPTY));
                nameText = StyledText.fromString("Shiny ").append(nameText);
            }

            if (showPerfectOrDefective.get()) {
                if (wynnItem instanceof IdentifiableItemProperty<?, ?> identifiableItemProperty) {
                    if (identifiableItemProperty.isPerfect()) {
                        nameText = StyledText.fromComponent(
                                ComponentUtils.makeRainbowStyle("Perfect " + nameText.getString(), true));
                    } else if (identifiableItemProperty.isDefective()) {
                        nameText = StyledText.fromComponent(
                                ComponentUtils.makeCrimsonStyle("Defective " + nameText.getString(), true));
                    }
                }
            }

            if (wynnItem instanceof MountItem mountItem) {
                nameText = StyledText.fromString(StringUtils.toPossessive(mountItem.getName()) + " "
                        + (mountItem.isSummonItem()
                                ? mountItem.getMountType().getSummonItemName()
                                : mountItem.getMountType().getMountItemName()));
            }
        }

        Style style = Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE).withColor(ChatFormatting.GOLD);

        if (wynnItem instanceof GearTierItemProperty tierItemProperty) {
            style = style.withColor(tierItemProperty.getGearTier().getChatFormatting());
        }

        FakeItemStack itemStack = new FakeItemStack(wynnItem, ItemSourceType.ENCODED, "From chat");
        style = style.withHoverEvent(new HoverEvent.ShowItem(itemStack));
        if (itemStack.hasTooltipPages()) {
            style = style.withClickEvent(new ClickEvent.Custom(CHANGE_ITEM_PAGE, Optional.empty()));
        }

        // Add the item name
        StyledText appendedNameText =
                StyledText.fromComponent(Component.empty().withStyle(style).append(nameText.getComponent()));
        parts.addAll(Arrays.stream(appendedNameText.getPartsAsTextArray())
                .map(StyledText::getFirstPart)
                .map(part -> part.withStyle(part.getPartStyle().withUnderlined(true)))
                .toList());

        return parts;
    }

    private void makeChatPrompt(WynnItem wynnItem) {
        // Do NOT share the name of the item encoded, as we share the item name in the chat message
        EncodingSettings encodingSettings =
                new EncodingSettings(Models.ItemEncoding.extendedIdentificationEncoding.get(), false);
        ErrorOr<EncodedByteBuffer> errorOrEncodedByteBuffer =
                Models.ItemEncoding.encodeItem(wynnItem, encodingSettings);
        if (errorOrEncodedByteBuffer.hasError()) {
            WynntilsMod.error("Failed to encode item: " + errorOrEncodedByteBuffer.getError());
            McUtils.sendErrorToClient(
                    I18n.get("feature.wynntils.chatItem.chatItemErrorEncode", errorOrEncodedByteBuffer.getError()));
            return;
        }

        if (WynntilsMod.isDevelopmentEnvironment()) {
            WynntilsMod.info("Encoded item: " + errorOrEncodedByteBuffer.getValue());
            WynntilsMod.info("Encoded item UTF-16: "
                    + errorOrEncodedByteBuffer.getValue().toUtf16String());
        }

        McUtils.sendWynntilsPrefixMessage(Component.translatable("feature.wynntils.chatItem.chatItemMessage")
                .withStyle(ChatFormatting.DARK_GREEN)
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(s -> s.withClickEvent(new ClickEvent.CopyToClipboard(
                        Models.ItemEncoding.makeItemString(wynnItem, errorOrEncodedByteBuffer.getValue()))))
                .withStyle(s -> s.withHoverEvent(
                        new HoverEvent.ShowText(Component.translatable("feature.wynntils.chatItem.chatItemTooltip")
                                .withStyle(ChatFormatting.DARK_AQUA)))));
    }

    private void requestNextPage() {
        pageParser.setExpectedPage((pageParser.item().currentPage() + 1) % 3);
        pageParser.setWaitTicks(0);
        pageParser.state().sent();

        if (pageParser.sharingScreen() != null && !pageParser.timedOut()) {
            pageParser.sharingScreen().setParsingPage(pageParser.expectedPage());
        }

        ContainerUtils.pressSwapHandsOnSlot(
                pageParser.menu().containerId,
                pageParser.menu().getStateId(),
                pageParser.slot().index,
                pageParser.menu().getCarried());
    }

    private void finishSharing(Slot hoveredSlot, WynnItem item, boolean share) {
        if (share) {
            if (showSharingScreen.get()) {
                McUtils.setScreen(ItemSharingScreen.create(item, hoveredSlot.getItem()));
            } else {
                makeChatPrompt(item);
            }
        } else {
            ItemStack itemStackToSave = hoveredSlot.getItem();

            // Gear items can have their item changed by cosmetics so we need to get their original item
            // FIXME: Does not work for crafted gear
            if (item instanceof GearItem gearItem) {
                itemStackToSave = new FakeItemStack(
                        gearItem, ItemSourceType.ENCODED, "From " + McUtils.playerName() + "'s Item Record");
            }

            // Item name is passed in since it is lost in the instanceof check above and looks nicer
            // saying "Saved Gale's Force to your Item Record" than "Saved Bow to your Item Record"
            Services.ItemRecord.saveItem(
                    item, itemStackToSave, hoveredSlot.getItem().getHoverName());
        }
    }

    private static final class PageParser {
        private final Screen screen;
        private final ItemSharingScreen sharingScreen;
        private final AbstractContainerMenu menu;
        private final Slot slot;
        private final PagedItemProperty item;
        private final boolean share;
        private final PageParserState state = new PageParserState();

        private int remainingSwaps = 3;
        private int expectedPage;
        private int waitTicks;
        private boolean timedOut;

        private PageParser(
                Screen screen,
                AbstractContainerMenu menu,
                Slot slot,
                PagedItemProperty item,
                boolean share,
                ItemSharingScreen sharingScreen) {
            this.screen = screen;
            this.sharingScreen = sharingScreen;
            this.menu = menu;
            this.slot = slot;
            this.item = item;
            this.share = share;
        }

        private ItemSharingScreen sharingScreen() {
            return sharingScreen;
        }

        private AbstractContainerMenu menu() {
            return menu;
        }

        private Slot slot() {
            return slot;
        }

        private PagedItemProperty item() {
            return item;
        }

        private boolean share() {
            return share;
        }

        private PageParserState state() {
            return state;
        }

        private int remainingSwaps() {
            return remainingSwaps;
        }

        private void setRemainingSwaps(int remainingSwaps) {
            this.remainingSwaps = remainingSwaps;
        }

        private int expectedPage() {
            return expectedPage;
        }

        private void setExpectedPage(int expectedPage) {
            this.expectedPage = expectedPage;
        }

        private int waitTicks() {
            return waitTicks;
        }

        private void setWaitTicks(int waitTicks) {
            this.waitTicks = waitTicks;
        }

        private boolean timedOut() {
            return timedOut;
        }

        private void setTimedOut(boolean timedOut) {
            this.timedOut = timedOut;
        }

        private boolean isScreenCurrent() {
            return McUtils.screen() == (sharingScreen != null ? sharingScreen : screen);
        }
    }

    private static final class PageParserState {
        private static final int PAGE_DELAY_TICKS = 6;
        private static final int RETRY_DELAY_TICKS = 10;
        private static final int MAX_ATTEMPTS = 3;

        private boolean waitingToSend = true;
        private boolean readyToSend = true;
        private boolean readyToRetry;
        private TickSchedulerManager.ScheduledTask scheduledTask;
        private int attempts;

        public boolean isWaitingToSend() {
            return waitingToSend;
        }

        public boolean canSend() {
            return waitingToSend && readyToSend;
        }

        public void sent() {
            cancel();

            waitingToSend = false;
            attempts++;
        }

        public void acknowledged() {
            cancel();

            waitingToSend = true;
            attempts = 0;
            scheduledTask = Managers.TickScheduler.scheduleLater(
                    () -> {
                        scheduledTask = null;
                        readyToSend = true;
                    },
                    PAGE_DELAY_TICKS);
        }

        public void unchangedResponse() {
            if (waitingToSend || attempts >= MAX_ATTEMPTS) return;

            cancel();
            scheduledTask = Managers.TickScheduler.scheduleLater(
                    () -> {
                        scheduledTask = null;
                        readyToRetry = true;
                    },
                    RETRY_DELAY_TICKS);
        }

        public boolean shouldRetry() {
            return !waitingToSend && attempts < MAX_ATTEMPTS && readyToRetry;
        }

        public void cancel() {
            if (scheduledTask != null) {
                Managers.TickScheduler.cancel(scheduledTask);
                scheduledTask = null;
            }

            readyToSend = false;
            readyToRetry = false;
        }
    }
}
