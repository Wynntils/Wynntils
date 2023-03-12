/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.event.FeatureStateChangeEvent;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.features.GammabrightFeature;
import com.wynntils.features.LootrunFeature;
import com.wynntils.features.TerritoryDefenseMessageFeature;
import com.wynntils.features.TranslationFeature;
import com.wynntils.features.chat.ChatCoordinatesFeature;
import com.wynntils.features.chat.ChatItemFeature;
import com.wynntils.features.chat.ChatMentionFeature;
import com.wynntils.features.chat.ChatTabsFeature;
import com.wynntils.features.chat.ChatTimestampFeature;
import com.wynntils.features.chat.DialogueOptionOverrideFeature;
import com.wynntils.features.chat.InfoMessageFilterFeature;
import com.wynntils.features.chat.TradeMarketAutoOpenChatFeature;
import com.wynntils.features.chat.TradeMarketPriceConversionFeature;
import com.wynntils.features.combat.AbbreviateMobHealthFeature;
import com.wynntils.features.combat.CombatXpGainMessageFeature;
import com.wynntils.features.combat.FixCastingSpellsFromInventoryFeature;
import com.wynntils.features.combat.HealthPotionBlockerFeature;
import com.wynntils.features.combat.HorseMountFeature;
import com.wynntils.features.combat.LowHealthVignetteFeature;
import com.wynntils.features.combat.MythicBlockerFeature;
import com.wynntils.features.combat.MythicBoxScalerFeature;
import com.wynntils.features.combat.PreventTradesDuelsFeature;
import com.wynntils.features.combat.QuickCastFeature;
import com.wynntils.features.commands.AddCommandExpansionFeature;
import com.wynntils.features.commands.CommandAliasesFeature;
import com.wynntils.features.commands.CustomCommandKeybindsFeature;
import com.wynntils.features.commands.FilterAdminCommandsFeature;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.ItemDebugTooltipsFeature;
import com.wynntils.features.debug.LogItemInfoFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.features.inventory.BulkBuyFeature;
import com.wynntils.features.inventory.ContainerSearchFeature;
import com.wynntils.features.inventory.DurabilityArcFeature;
import com.wynntils.features.inventory.EmeraldPouchHotkeyFeature;
import com.wynntils.features.inventory.ExtendedItemCountFeature;
import com.wynntils.features.inventory.HidePotionGlintFeature;
import com.wynntils.features.inventory.IngredientPouchHotkeyFeature;
import com.wynntils.features.inventory.InventoryEmeraldCountFeature;
import com.wynntils.features.inventory.ItemFavoriteFeature;
import com.wynntils.features.inventory.ItemHighlightFeature;
import com.wynntils.features.inventory.ItemLockFeature;
import com.wynntils.features.inventory.ItemScreenshotFeature;
import com.wynntils.features.inventory.ItemTextOverlayFeature;
import com.wynntils.features.inventory.ReplaceRecipeBookFeature;
import com.wynntils.features.inventory.UnidentifiedItemIconFeature;
import com.wynntils.features.map.BeaconBeamFeature;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.features.map.MapFeature;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.features.map.WorldWaypointDistanceFeature;
import com.wynntils.features.overlays.ArrowShieldTrackingFeature;
import com.wynntils.features.overlays.AuraTimerOverlayFeature;
import com.wynntils.features.overlays.CustomBarsOverlayFeature;
import com.wynntils.features.overlays.GameNotificationOverlayFeature;
import com.wynntils.features.overlays.GuildAttackTimerOverlayFeature;
import com.wynntils.features.overlays.InfoBoxFeature;
import com.wynntils.features.overlays.MobTotemTrackingFeature;
import com.wynntils.features.overlays.NpcDialogueOverlayFeature;
import com.wynntils.features.overlays.ObjectivesOverlayFeature;
import com.wynntils.features.overlays.PowderSpecialBarOverlayFeature;
import com.wynntils.features.overlays.QuestInfoOverlayFeature;
import com.wynntils.features.overlays.ShamanMasksOverlayFeature;
import com.wynntils.features.overlays.ShamanTotemTrackingFeature;
import com.wynntils.features.overlays.SpellCastRenderFeature;
import com.wynntils.features.overlays.StatusOverlayFeature;
import com.wynntils.features.players.AutoJoinPartyFeature;
import com.wynntils.features.players.CustomNametagRendererFeature;
import com.wynntils.features.players.GearViewerFeature;
import com.wynntils.features.players.HadesFeature;
import com.wynntils.features.players.PartyManagementScreenFeature;
import com.wynntils.features.players.PlayerArmorHidingFeature;
import com.wynntils.features.players.PlayerGhostTransparencyFeature;
import com.wynntils.features.players.WynntilsCosmeticsFeature;
import com.wynntils.features.redirects.AbilityRefreshRedirectFeature;
import com.wynntils.features.redirects.BlacksmithRedirectFeature;
import com.wynntils.features.redirects.ChatRedirectFeature;
import com.wynntils.features.redirects.InventoryRedirectFeature;
import com.wynntils.features.redirects.TerritoryMessageRedirectFeature;
import com.wynntils.features.tooltips.ItemCompareFeature;
import com.wynntils.features.tooltips.ItemGuessFeature;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.features.tooltips.TooltipFittingFeature;
import com.wynntils.features.tooltips.TooltipVanillaHideFeature;
import com.wynntils.features.ui.AbilityTreeScrollFeature;
import com.wynntils.features.ui.AutoApplyResourcePackFeature;
import com.wynntils.features.ui.CosmeticsPreviewFeature;
import com.wynntils.features.ui.CustomCharacterSelectionScreenFeature;
import com.wynntils.features.ui.LobbyUptimeFeature;
import com.wynntils.features.ui.SoulPointTimerFeature;
import com.wynntils.features.ui.WynncraftButtonFeature;
import com.wynntils.features.ui.WynncraftPauseScreenFeature;
import com.wynntils.features.ui.WynntilsQuestBookFeature;
import com.wynntils.features.wynntils.ChangelogFeature;
import com.wynntils.features.wynntils.CommandsFeature;
import com.wynntils.features.wynntils.DataStorageFeature;
import com.wynntils.features.wynntils.FixPacketBugsFeature;
import com.wynntils.features.wynntils.UpdatesFeature;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.reflect.FieldUtils;

/** Loads {@link Feature}s */
public final class FeatureManager extends Manager {
    private static final Map<Feature, FeatureState> FEATURES = new LinkedHashMap<>();

    public FeatureManager(CrashReportManager crashReport, KeyBindManager keyBind, OverlayManager overlay) {
        super(List.of(crashReport, keyBind, overlay));
    }

    public void init() {
        // debug
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new ItemDebugTooltipsFeature());
        registerFeature(new LogItemInfoFeature());
        registerFeature(new PacketDebuggerFeature());

        // always on
        registerFeature(new LootrunFeature());
        registerFeature(new DataStorageFeature());

        // user
        registerFeature(new AbbreviateMobHealthFeature());
        registerFeature(new AbilityRefreshRedirectFeature());
        registerFeature(new AbilityTreeScrollFeature());
        registerFeature(new AddCommandExpansionFeature());
        registerFeature(new ArrowShieldTrackingFeature());
        registerFeature(new AuraTimerOverlayFeature());
        registerFeature(new AutoApplyResourcePackFeature());
        registerFeature(new AutoJoinPartyFeature());
        registerFeature(new BeaconBeamFeature());
        registerFeature(new BlacksmithRedirectFeature());
        registerFeature(new BulkBuyFeature());
        registerFeature(new ChangelogFeature());
        registerFeature(new ChatCoordinatesFeature());
        registerFeature(new ChatItemFeature());
        registerFeature(new ChatMentionFeature());
        registerFeature(new ChatRedirectFeature());
        registerFeature(new ChatTabsFeature());
        registerFeature(new ChatTimestampFeature());
        registerFeature(new CombatXpGainMessageFeature());
        registerFeature(new CommandAliasesFeature());
        registerFeature(new CommandsFeature());
        registerFeature(new ContainerSearchFeature());
        registerFeature(new CosmeticsPreviewFeature());
        registerFeature(new CustomBarsOverlayFeature());
        registerFeature(new CustomCharacterSelectionScreenFeature());
        registerFeature(new CustomCommandKeybindsFeature());
        registerFeature(new CustomNametagRendererFeature());
        registerFeature(new DialogueOptionOverrideFeature());
        registerFeature(new DurabilityArcFeature());
        registerFeature(new EmeraldPouchHotkeyFeature());
        registerFeature(new ExtendedItemCountFeature());
        registerFeature(new FilterAdminCommandsFeature());
        registerFeature(new FixCastingSpellsFromInventoryFeature());
        registerFeature(new FixPacketBugsFeature());
        registerFeature(new GameNotificationOverlayFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new GearViewerFeature());
        registerFeature(new GuildAttackTimerOverlayFeature());
        registerFeature(new GuildMapFeature());
        registerFeature(new HadesFeature());
        registerFeature(new HealthPotionBlockerFeature());
        registerFeature(new HidePotionGlintFeature());
        registerFeature(new InfoBoxFeature());
        registerFeature(new InfoMessageFilterFeature());
        registerFeature(new IngredientPouchHotkeyFeature());
        registerFeature(new InventoryEmeraldCountFeature());
        registerFeature(new InventoryRedirectFeature());
        registerFeature(new ItemCompareFeature());
        registerFeature(new ItemFavoriteFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new ItemHighlightFeature());
        registerFeature(new ItemLockFeature());
        registerFeature(new ItemScreenshotFeature());
        registerFeature(new HorseMountFeature());
        registerFeature(new ItemStatInfoFeature());
        registerFeature(new ItemTextOverlayFeature());
        registerFeature(new LobbyUptimeFeature());
        registerFeature(new LowHealthVignetteFeature());
        registerFeature(new MapFeature());
        registerFeature(new MinimapFeature());
        registerFeature(new MobTotemTrackingFeature());
        registerFeature(new MythicBlockerFeature());
        registerFeature(new MythicBoxScalerFeature());
        registerFeature(new NpcDialogueOverlayFeature());
        registerFeature(new ObjectivesOverlayFeature());
        registerFeature(new PartyManagementScreenFeature());
        registerFeature(new PlayerArmorHidingFeature());
        registerFeature(new PlayerGhostTransparencyFeature());
        registerFeature(new PowderSpecialBarOverlayFeature());
        registerFeature(new PreventTradesDuelsFeature());
        registerFeature(new QuestInfoOverlayFeature());
        registerFeature(new QuickCastFeature());
        registerFeature(new ReplaceRecipeBookFeature());
        registerFeature(new ShamanMasksOverlayFeature());
        registerFeature(new ShamanTotemTrackingFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new SpellCastRenderFeature());
        registerFeature(new StatusOverlayFeature());
        registerFeature(new TerritoryDefenseMessageFeature());
        registerFeature(new TerritoryMessageRedirectFeature());
        registerFeature(new TooltipFittingFeature());
        registerFeature(new TooltipVanillaHideFeature());
        registerFeature(new TradeMarketAutoOpenChatFeature());
        registerFeature(new TradeMarketPriceConversionFeature());
        registerFeature(new TranslationFeature());
        registerFeature(new UnidentifiedItemIconFeature());
        registerFeature(new UpdatesFeature());
        registerFeature(new WorldWaypointDistanceFeature());
        registerFeature(new WynncraftButtonFeature());
        registerFeature(new WynncraftPauseScreenFeature());
        registerFeature(new WynntilsCosmeticsFeature());
        registerFeature(new WynntilsQuestBookFeature());

        // Reload Minecraft's config files so our own keybinds get loaded
        // This is needed because we are late to register the keybinds,
        // but we cannot move it earlier to the init process because of I18n
        synchronized (McUtils.options()) {
            McUtils.mc().options.load();
        }

        addCrashCallbacks();
    }

    private void registerFeature(Feature feature) {
        FEATURES.put(feature, FeatureState.DISABLED);

        try {
            initializeFeature(feature);
        } catch (AssertionError ae) {
            WynntilsMod.error("Fix i18n for " + feature.getClass().getSimpleName(), ae);
            if (WynntilsMod.isDevelopmentEnvironment()) {
                System.exit(1);
            }
        } catch (Throwable exception) {
            // Log and fail gracefully, don't make other features fail to init
            WynntilsMod.error(
                    "Failed to initialize feature " + feature.getClass().getSimpleName(), exception);
        }
    }

    private void initializeFeature(Feature feature) {
        Class<? extends Feature> featureClass = feature.getClass();

        // Instance field
        try {
            Field instanceField = FieldUtils.getDeclaredField(featureClass, "INSTANCE", true);
            if (instanceField != null) instanceField.set(null, feature);
        } catch (Exception e) {
            WynntilsMod.error("Failed to create instance object in " + featureClass.getName(), e);
            return;
        }

        // Set feature category
        ConfigCategory configCategory = feature.getClass().getAnnotation(ConfigCategory.class);
        Category category = configCategory != null ? configCategory.value() : Category.UNCATEGORIZED;
        feature.setCategory(category);

        // Register key binds
        Managers.KeyBind.discoverKeyBinds(feature);

        // Determine if feature should be enabled & set default enabled value for user features
        boolean startDisabled = featureClass.isAnnotationPresent(StartDisabled.class);
        if (feature instanceof UserFeature userFeature) {
            userFeature.userEnabled = !startDisabled;
        }

        Managers.Overlay.discoverOverlays(feature);
        Managers.Overlay.discoverOverlayGroups(feature);

        // Assert that the feature name is properly translated
        assert !feature.getTranslatedName().startsWith("feature.wynntils.");

        if (feature instanceof UserFeature userFeature) {
            if (!userFeature.userEnabled) return; // not enabled by user

            enableFeature(feature);
        } else if (!startDisabled) {
            enableFeature(feature);
        }
    }

    public void enableFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to enable an unregistered feature: " + feature);
        }

        FeatureState state = FEATURES.get(feature);

        if (state != FeatureState.DISABLED && state != FeatureState.CRASHED) return;

        WynntilsMod.postEvent(new FeatureStateChangeEvent.Enabled(feature));

        FEATURES.put(feature, FeatureState.ENABLED);

        WynntilsMod.registerEventListener(feature);

        Managers.Overlay.enableOverlays(feature);

        Managers.KeyBind.enableFeatureKeyBinds(feature);
    }

    public void disableFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to disable an unregistered feature: " + feature);
        }

        FeatureState state = FEATURES.get(feature);

        if (state != FeatureState.ENABLED) return;

        WynntilsMod.postEvent(new FeatureStateChangeEvent.Disabled(feature));

        FEATURES.put(feature, FeatureState.DISABLED);

        WynntilsMod.unregisterEventListener(feature);

        Managers.KeyBind.disableFeatureKeyBinds(feature);
    }

    public void crashFeature(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException("Tried to crash an unregistered feature: " + feature);
        }

        disableFeature(feature);

        // Sending both Crashed and Disabled events is intentional, as crashing disables the feature
        WynntilsMod.postEvent(new FeatureStateChangeEvent.Crashed(feature));

        FEATURES.put(feature, FeatureState.CRASHED);
    }

    public FeatureState getFeatureState(Feature feature) {
        if (!FEATURES.containsKey(feature)) {
            throw new IllegalArgumentException(
                    "Feature " + feature + " is not registered, but was was queried for its state");
        }

        return FEATURES.get(feature);
    }

    public boolean isEnabled(Feature feature) {
        return getFeatureState(feature) == FeatureState.ENABLED;
    }

    public List<Feature> getFeatures() {
        return FEATURES.keySet().stream().toList();
    }

    public Optional<Feature> getFeatureFromString(String featureName) {
        return getFeatures().stream()
                .filter(feature -> feature.getShortName().equals(featureName))
                .findFirst();
    }

    private void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Features", () -> {
            StringBuilder result = new StringBuilder();

            for (Feature feature : FEATURES.keySet()) {
                if (feature.isEnabled()) {
                    result.append("\n\t\t").append(feature.getTranslatedName());
                }
            }

            return result.toString();
        });
    }
}
